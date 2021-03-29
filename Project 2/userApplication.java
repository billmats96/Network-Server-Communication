//   *   * Computer Networks IÉ   *   * Experimental Virtual Lab   *   * 
// Author: Matsoukas Vasileios
//         Undergraduate Student, Department of Electrical and Computer Engineering 
//		   Aristotle University of Thessaloniki, Greece



import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.io.*;


import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.swing.*;


public class userApplication {
	
	
	//**** Define constants used in application ****
	
	public static final int serverPort= SERVER_PORT;
	public static final int clientPort= CLIENT_PORT;
	public static final byte[] hostIP = HOST_IP; 
	public static final byte[] ifaIP= { (byte) 178,(byte)33,(byte)15, (byte)194 };
	public static final byte[] transitionSportsIP= { (byte) 178,(byte)238,(byte)226, (byte)116 };
	public static final byte[] fordIP = { (byte) 62,(byte)38,(byte) 6, (byte)99 };
	public static final byte[] gazzetaIP = { (byte) 176,(byte)9,(byte) 111, (byte)98};
	public static final byte[] meteoIP = { (byte) 46,(byte)198,(byte) 143, (byte)226 };
    	public static final byte[] ethmmyIP = { (byte) 155,(byte)207,(byte) 33, (byte)32 };
	public static final byte[] artemisIP = { (byte) 155,(byte)207,(byte) 26, (byte)179};
	public static final byte[] clientIP={ (byte)94,(byte)67,(byte)102,(byte)255 };
	public static final String echo = "echo_request_code=E0950";
	public static final String echoZero = "echo_request_code=E0000";
	public static final String echoTemp = "echo_request_code=E3231T00"; 
	public static final String image = "image_request_code=M9555CAM=FIXUDP=1024";
	public static final String image2= "image_request_code=M9555CAM=PTZFLOW=ONUDP=1024"; 
	public static final int imagePacket =1024;  //Length of image packet
	public static final String sound128 = "sound_request_code=A2973F999";  
	public static final String sound128freq = "sound_request_code=A2973T999";
	public static final String sound132 = "sound_request_code=A2973AQF999";
	public static final int audioPacket = 999;
	public static final int audioPacketLength128 = 128;
	public static final int audioPacketLength132 = 132;
	public static final String ithakiCopter = "ithaki_copter_code=Q9857";  //not necessary to obtain copter data 
	public static final String vehicleOBD = "obd_request_code=V6463";      //not necessary to obtain OBD data
	public static final byte highBitMask = (byte) 0x000000F0;
	public static final byte lowBitMask = (byte) 0x0000000F;
		
	
	

	public static ArrayList<Integer> echoTemp(DatagramSocket s, DatagramSocket r,InetAddress hostAddress) throws IOException
	{
		ArrayList<Integer> temperatureList = new ArrayList<Integer>();
		String packetInfo = echoTemp;
		byte[] rxbuffer = new byte[2048];
		DatagramPacket q = new DatagramPacket(rxbuffer,rxbuffer.length);
		byte[] txbuffer=packetInfo.getBytes();
		DatagramPacket p = new DatagramPacket(txbuffer,txbuffer.length,hostAddress,serverPort);
		int y=0;
		//Change the value of k depending of how many measurements you want to take from sensor.
		for(int k=0; k<8; k++)
		{
			s.send(p);
			try 
			{
				r.receive(q);
				String message = new String(rxbuffer,0,q.getLength()); 
				y=Integer.parseInt(message.substring(44,46));
				temperatureList.add(y);
				System.out.println(message);
			}
			catch(Exception x)
			{
				 System.out.println(x); 
				 break;
			}	
		}		
		return temperatureList;
	}
	public static ArrayList<ArrayList<Integer>> echoPacket(DatagramSocket s, DatagramSocket r,InetAddress hostAddress, int duration, int delay) throws IOException
	{
		
		ArrayList<Integer> responseTime = new ArrayList<Integer>();
		ArrayList<Integer> movingAverage = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> total = new ArrayList<ArrayList<Integer>>();
		String packetInfo=echo;
		String packetInfo1=echoZero; 
		byte[] rxbuffer = new byte[2048];
		DatagramPacket q = new DatagramPacket(rxbuffer,rxbuffer.length);
		long sum=0; 
		int  movingAvgWindow=8000,i=0,k=1;
		long clockAvg=System.currentTimeMillis();
		long end1, timeElapsed,start1=System.currentTimeMillis();
		byte[] txbuffer;
		
		do{
			
			//Choose whether to request packets with delay or without delay.
			if(delay==1) 
				txbuffer = packetInfo.getBytes();
			else
				txbuffer = packetInfo1.getBytes();
		
			DatagramPacket p = new DatagramPacket(txbuffer,txbuffer.length,hostAddress,serverPort);
			long start=System.currentTimeMillis();
			s.send(p);  
			try 
			{
				r.receive(q);
				String message = new String(rxbuffer,0,q.getLength());  
				System.out.println(message);
				System.out.println("LENGTH IS: "+q.getLength());
			}
			catch(Exception x)
			{
				 System.out.println(x); 
				 break;
			}
			long end=System.currentTimeMillis();
			System.out.println("Time needed for the packet:"+ (end-start)+"ms");
			
			
			responseTime.add((int)(end-start));
			if ((System.currentTimeMillis()-clockAvg)>=1000) 
			{
				clockAvg=System.currentTimeMillis();
				k++; //count how many times we took samples of moving average
				sum=0; 
				i=0;
				System.out.println("Clock = " + (System.currentTimeMillis()-clockAvg));
				System.out.println("K = " + k); 
				do
				{	
						
					if(responseTime.size()>0)
					{
						sum+=responseTime.get(responseTime.size()-i-1);
						i++;
					}
					else
					{
						System.out.println("Index out of bounds!!");
						
					}
				}while(sum<=movingAvgWindow && (responseTime.size()-i)>0);
						
				System.out.println("Num of total packets= "+responseTime.size());
				System.out.println("Num of packets in moving average= "+i);
				movingAverage.add((int)i);		
			}
			System.out.println();
			end1=System.currentTimeMillis();
			timeElapsed=end1-start1;
		}while(timeElapsed<=duration);
		total.add(responseTime);
	    total.add(movingAverage);
	    return total;
	}
	public static int[] toIntArray(ArrayList<Integer> input)
	{
		int[] returnedArray=new int[input.size()];
	    for(int i=0; i<input.size(); i++)
	    {
	     	 System.out.println(input.get(i));
	    	 returnedArray[i]=(int)input.get(i);  //copy response times to array
	    }
	    return returnedArray;
	}
	public static void textSave(int[] dataArray, String filename) throws IOException
	  {
		  try {
			BufferedWriter file = new BufferedWriter(new FileWriter(filename));
			  for (int i = 0; i < dataArray.length; i++) {
			       file.write(String.valueOf(dataArray[i]));
			       file.write(String.format("%n"));
			  }
			      file.close();
		} catch (Exception e) {
			System.out.println("Error - " + e.toString());
		}
	  }
	public static void textSave2(float[] dataArray, String filename)throws IOException
	  {
		  try {
			BufferedWriter file = new BufferedWriter(new FileWriter(filename));
			  for (int i = 0; i < dataArray.length; i++) {
			       file.write(String.valueOf(dataArray[i]));
			       file.write(String.format("%n"));
			  }
			      file.close();
		} catch (Exception e) {
			System.out.println("Error - " + e.toString());
		}
	  }
	public static byte[] imageDataDownload(DatagramSocket s, DatagramSocket r,InetAddress hostAddress,String packetInfo) throws IOException
	{
		byte[] tempArray = new byte[100000];
		byte[] rxbuffer = new byte[imagePacket]; 
		byte[] txbuffer;
		DatagramPacket q = new DatagramPacket(rxbuffer,rxbuffer.length);
		txbuffer=packetInfo.getBytes();
		DatagramPacket p = new DatagramPacket(txbuffer,txbuffer.length,hostAddress,serverPort);
		
		s.send(p); 
		int counter=0;
		boolean flag2=false;
		byte k,m;
		
		for(;;)
		{
			
			try
			{
				r.receive(q);
				
				txbuffer="NEXT".getBytes();
				DatagramPacket PE = new DatagramPacket(txbuffer,txbuffer.length,hostAddress,serverPort);
				s.send(PE);
				
				txbuffer=packetInfo.getBytes();
				
				System.out.print("LENGTH OF Q IS: "+q.getLength()+" ");
				System.out.println((byte)rxbuffer[0]);

				for(int i=0; i<rxbuffer.length-1; i++)
				{
					tempArray[counter*imagePacket+i]=rxbuffer[i];
					k=(byte)rxbuffer[i];
					m=(byte)rxbuffer[i+1];
					if(i==rxbuffer.length-2) tempArray[counter*imagePacket+i+1]=m;
					if(k==-1 && m==-39)
					{
						System.out.println("telos----------");
						flag2=true;
					}
				}
				counter++;
				System.out.println(counter);
				if (flag2) break;
			
			}
			catch(Exception x)
			{
				System.out.println(x); 
				break;
			}
		}
		System.out.println("CONFIRM Q LENGTH"+q.getLength());
		return Arrays.copyOfRange(tempArray, 0, (counter-1)*imagePacket + q.getLength()); //arraysCopy of range
		
	}
	public static byte[] imageExtract(byte[] array)
	  {
		  System.out.println();
		  System.out.println("I received an array with length "+array.length);
		  System.out.println();
		  System.out.println("The first two and last two elements of the array");
		  System.out.println(array[0]);
		  System.out.println(array[1]);
		  System.out.println(array[array.length-2]);
		  System.out.println(array[array.length-1]);
		
		  int startIndex=0;
		  int finishIndex=0;
		  
		  startIndex=checkInitialBytes(array);
		  finishIndex=checkEndBytes(array);
		  System.out.println("start index="+ startIndex);
		  System.out.println("finish index="+ finishIndex);
		  return Arrays.copyOfRange(array,startIndex,finishIndex+1);
	  }
    public static void imageSave(byte[] imageArray, String imageName)
	  {
		    FileOutputStream image = null;
		    File file;
		    try
		    {
		      //Specify the file path here
			  file = new File("C:/users/desktop/data/"+imageName+".jpg");
			  image = new FileOutputStream(file);
		      // This logic will check whether the file exists or not. If the file is not found at the specified location it will create a new file
			  if (!file.exists()) 
			     file.createNewFile();

			  //String content cannot be directly written into a file. It needs to be converted into bytes
			  image.write(imageArray);
			  image.flush();
			  System.out.println("File Written Successfully");
		    } 
		     catch (IOException ioe)
		     {
		    	 ioe.printStackTrace();
		     }
			 
		     finally
		     {
			   try
			   {
			     if (image != null) 
			     {
			    	 image.close();
			     }
		       } 
			   catch (IOException ioe)
			   {
			     System.out.println("Error in closing the Stream");
			   }
		     }
	  }
	public static int checkInitialBytes(byte[] array)
	  {
		  int i=0, startIndex=0;
		  for(;;)
		  {
			  try 
			  {
			    while(array[i]!=-1)
			  	{
			       i++;  
			  	}
			  	if(array[i+1]==-40)
			  	{
			  	   startIndex=i;
			  	   break;
			  	} 
			  }
			  catch(Exception x)
			  {
				   System.out.println(x);
			  }
		  }
		  return startIndex;
	  }
	public static int checkEndBytes(byte[] array)
	  {
		  int j=1, finishIndex=array.length-1;
		  for(;;)
		  {
			 try 
			 {
				while(array[array.length-j-1]!=-1)
				{
				   j++;    		
				}
				if(array[array.length-j]==-39)
				{
					finishIndex=array.length-j;
				    break;
				}
			} 
			catch (Exception x) 
			 {
				System.out.println(x);
			}
			
		  }
		  return finishIndex;
	  }
	public static void videoStream(DatagramSocket s, DatagramSocket r,InetAddress hostAddress,long duration) throws IOException
	{
		//Setup GUI
		ImageIcon icon = new ImageIcon();
        JLabel label = new JLabel(icon);
        JFrame mainFrame = new JFrame();
        mainFrame.setLocation(400, 100);
        mainFrame.setTitle("Live Video Streaming");
        mainFrame.getContentPane().add(label);
        mainFrame.pack();
        mainFrame.setLocation(400, 100);
        mainFrame.setAlwaysOnTop(true);
        mainFrame.setVisible(true);
        
        //Measure throughput
        ArrayList<Integer> throughput = new ArrayList<Integer>();
        long start1,start=System.currentTimeMillis(),end,timeElapsed=0;
        //int duration=4*60*1000; //video stream duration
        
        //Video Streaming by continuously downloading and showing images
		do
		{	
			start1=System.currentTimeMillis();
			byte[] imageArray = imageDataDownload(s,r,hostAddress,image);
			imageArray = imageExtract(imageArray);
			imageSave(imageArray,"TEMP");
			end=System.currentTimeMillis();
			
			throughput.add((int)((imageArray.length*8)/((end-start1)/1000)));
			
			
			ByteArrayInputStream temp = new ByteArrayInputStream(imageArray);
			BufferedImage newFrame = ImageIO.read(temp); 
	    
			icon.setImage(newFrame);
			label = new JLabel(icon);
			mainFrame.getContentPane().add(label);
			mainFrame.pack();
			
			end=System.currentTimeMillis();
			timeElapsed=end-start;
			
		}while(timeElapsed<=duration);
		
		//Save throughput data
		int [] throughputArray = new int[throughput.size()];
		throughputArray=toIntArray(throughput);
		textSave(throughputArray,"C:\\Users\\billm\\Desktop\\data\\imageThroughputbps"+imagePacket+".txt");
    
     }
	public static byte[][] audioDataDownload128 (DatagramSocket s, DatagramSocket r,InetAddress hostAddress,String packetInfo) throws IOException //download and processing
	{
		byte[][] audioData = new byte[audioPacket][audioPacketLength128];
		byte[] rxbuffer = new byte[audioPacketLength128];
		byte[] txbuffer = packetInfo.getBytes();
		DatagramPacket p = new DatagramPacket(txbuffer,txbuffer.length,hostAddress,serverPort);
		DatagramPacket q = new DatagramPacket(rxbuffer,rxbuffer.length);//,clientAddress,clientPort);
		for(int i=0; i<audioPacket; i++)
		{
			s.send(p);
			try
			{
				r.receive(q);
				for(int j=0; j<audioPacketLength128 ; j++)
				{
					audioData[i][j]=rxbuffer[j];
				}
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
		
		return audioData;
	}
	public static byte[][] audioDataDownload132 (DatagramSocket s, DatagramSocket r,InetAddress hostAddress,String packetInfo) throws IOException //download and processing
	{
		byte[][] audioData = new byte[999][132];
		byte[] rxbuffer = new byte[audioPacketLength132];
		byte[] txbuffer = packetInfo.getBytes();
		DatagramPacket p = new DatagramPacket(txbuffer,txbuffer.length,hostAddress,serverPort);
		DatagramPacket q = new DatagramPacket(rxbuffer,rxbuffer.length);//,clientAddress,clientPort);
		for(int i=0; i<audioPacket; i++)
		{
			s.send(p);
			try
			{
				r.receive(q);
				for(int j=0; j<audioPacketLength132 ; j++)
				{
					audioData[i][j]=rxbuffer[j];
				}
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
		
		return audioData;
	}
	public static byte[] audioAQDPCM(byte[][] audioData) throws IOException {

        byte[] audioBufferOut = new byte[2*(128*2)*audioPacket];

        int[][] samples = new int[audioPacket][128*2];
        int[] differences=new int[2*128*audioPacket];
        int[] samplesF=new int[2*128*audioPacket];
        int[] mean = new int[audioPacket];
        int[] step = new int[audioPacket];
        
        int lowNibble, highNibble;
        int temp,index=-1,B=0,M=0,c=0;
        byte lowB,highB,lowM,highM;
	    
        for (int i=0; i<audioPacket; i++) 
        {
        
        	//extract mean and step bytes
        	lowM=audioData[i][0];
            highM=audioData[i][1];
            lowB=audioData[i][2];
            highB=audioData[i][3];
            System.out.println("Low B is: " +lowB+ " High B is: "+highB);
            System.out.println("Low M is: " +lowM+ " High M is: "+highM);
           
            //caclulate mean and step from bytes above
            M  = highM & 0x000000FF;
            M *= 256;
            M += lowM & 0x000000FF;
            B  = highB & 0x000000FF;
            B *= 256;
            B += lowB & 0x000000FF;
            
            if(B<1 || B>65535)
            {
            	System.out.println("Invalid value of B!");
            	break;
            }
            if(M<0 || M>65535)
            {
            	System.out.println("Invalid value of M!");
            	break;
            }
            
            mean[i]=M;
            step[i]=B;
            System.out.println("B: "+B+"M: "+M+"mean[i]: "+ mean[i]+"step[i]: "+step[i]);
            index=-1;
            
            for (int j = 4; j < audioData[0].length; j++) { 
               
                
            	temp=(int)audioData[i][j];       //cast to integer 
                lowNibble =((temp&lowBitMask)-8 );  //take low nibble and subtract 8 
                highNibble = (((temp>>>4)&lowBitMask)-8); //take high nibble and subtract 8
               // System.out.println("highNibble: "+highNibble+" lowNibble: "+lowNibble);
                if(lowNibble<-8 || lowNibble>7)
                {
                	System.out.println("Error extracting low nibble");
                	break;
                }
                if(highNibble<-8 || highNibble>7)
                {
                	System.out.println("Error extracting high nibble");
                	break;
                }       
                
                samples[i][++index]=highNibble*B;  //multiply with step
                differences[index]=highNibble ;    //save differnce
                samples[i][++index]=lowNibble*B;   //multiply with step
                differences[index]=lowNibble; 	   //save differnce
            }
        } 
       
        //Recursively add samples array to extract the true samples.
        for (int i=0; i<audioPacket; i++)
        {
            for (int j=0; j<samples[0].length; j++) 
            {
                if (j>0)
                    samples[i][j] += samples[i][j-1];
                
                //Implement audio clipping in 16bit bit-depth samples for high fidelity audio.
                if(samples[i][j]>32767) samples[i][j]=32767;
                if(samples[i][j]<-32768) samples[i][j]=-32768; 
                
                //hold samples in a 1-D array to save results.
                samplesF[c]=samples[i][j];
                c++;
            }
        }

        
        index=-1;
        int max=-100000;
        int min=100000;

        for (int i=0; i<audioPacket; i++)
        {
            for (int j=0; j<samples[0].length; j++)
            {
               if (samples[i][j]>max) max=samples[i][j];               
               if (samples[i][j]<min) min=samples[i][j];

               audioBufferOut[++index]=(byte)samples[i][j];
               audioBufferOut[++index]=(byte)(samples[i][j]>>>8);  
            }
        }
        System.out.println("MAX SAMPLE: "+max +" MIN SAMPLE"+min);
        System.out.println("m: "+samples.length +" n: "+samples[0].length);
       
        textSave(differences,"C:\\Users\\Desktop\\data\\differencesAQDPCM.txt");
        textSave(samplesF,"C:\\Users\\Desktop\\data\\samplesAQDPCM.txt");
        textSave(mean,"C:\\Users\\Desktop\\data\\meanAQDPCM.txt");
        textSave(step,"C:\\Users\\Desktop\\data\\stepAQDPCM.txt");       
        
        return audioBufferOut;
    }
	public static byte[] audioDPCM(byte[][] audioData) throws LineUnavailableException, IOException
	{		
		int lowNibble,highNibble,temp;
		int b=1,index=-1;
		int[] differences= new int[2*128*audioPacket];
		int[] samples=new int[audioPacket*2*128];
		byte[] audioBufferOut = new byte[audioPacket*2*128];
		
		for(int i=0; i<audioPacket; i++)
		{
			try
			{
				for(int j=0; j<128 ; j++)
				{
					temp = (int) audioData[i][j];	//cast to integer
					lowNibble = ((temp&lowBitMask)-8);  //extract low nibble and subtract 8
	                highNibble = (((temp>>>4)&lowBitMask)-8); //extract high nibble and subtract 8 
	                samples[++index]=highNibble*b;	//multiply with step
	                differences[index]=highNibble;	//save difference
	                samples[++index]=lowNibble*b;	//multiply with step
	                differences[index]=lowNibble;	//save difference
	                
				}
					
			}
			catch(Exception x)
			{
				System.out.println(x);
			}
		}
		for(int k=0; k<samples.length; k++)
		{
			 if (k>0)
                 samples[k] += samples[k-1];
             
             //Implement audio clipping in 8bit bit-depth samples for high fidelity audio.
             if(samples[k]>127) samples[k]=127;
             if(samples[k]<-128)samples[k]=-128; 
             
           
		}
		int min=1000, max=-1000;
		for(int k=0; k<samples.length; k++)
		{
			//Find minimum and maximum value of samples
			if (samples[k]>max) max=samples[k];               
            if (samples[k]<min) min=samples[k];
			 
            audioBufferOut[k]=(byte)(samples[k]&0x000000FF);  
			 
   		}
		System.out.println("MAX SAMPLE: "+max +" MIN SAMPLE"+min);	
		textSave(differences,"C:\\Users\\Desktop\\data\\differencesDPCM.txt");
        textSave(samples,"C:\\Users\\Desktop\\data\\samplesDPCM.txt");
		return audioBufferOut;
	}
	public static void audioPlay(byte[] audioBufferOut,int sampleRate, int Q) throws LineUnavailableException
	{
		AudioFormat linearPCM = new AudioFormat(sampleRate,Q,1,true,false);  
		SourceDataLine lineOut = AudioSystem.getSourceDataLine(linearPCM);		 
		try
		{
			lineOut.open(linearPCM, audioBufferOut.length ); 
		} 
		catch (LineUnavailableException e) 
		{
			System.out.println(e);
		} 
		lineOut.start();
		lineOut.write(audioBufferOut,0,audioBufferOut.length); 
		lineOut.stop(); 
		lineOut.close(); 
	}
	public static void audioSave(byte[] audioBufferOut, String filename,int Q)
	{
		InputStream b_in = new ByteArrayInputStream(audioBufferOut);
		try {
		        DataOutputStream dos = new DataOutputStream(new FileOutputStream("C:\\Users\\billm\\Desktop\\f.bin"));
		        dos.write(audioBufferOut);
		        AudioFormat format = new AudioFormat(8000f, Q, 1, true, false);
		        AudioInputStream stream = new AudioInputStream(b_in, format,audioBufferOut.length);
		        File file = new File("C:\\Users\\Desktop\\data\\"+filename+".wav");
		        AudioSystem.write(stream, Type.WAVE, file);
		        System.out.println("File saved: "+file.getName()+", bytes: "+audioBufferOut.length);
		        dos.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
	public static void copterDataTelemetry() throws IOException, SocketException
	{
		DatagramSocket r_copter = new DatagramSocket(48038); //Listen for copter data packets in 48038 port
		r_copter.setSoTimeout(4000);
		byte[] rxbuffer = new byte[2048];
		DatagramPacket q = new DatagramPacket(rxbuffer,rxbuffer.length);
		ArrayList<String> copterDataList=new ArrayList<String>(); //Save all data packets in a List
		long t=System.currentTimeMillis(); //Keep track of time needed to receive a single packet
		
		//k determines how many response packets we will take from copter
		for(int k=0;k<100;k++)
		{
			
			try 
			{
				r_copter.receive(q);
				System.out.println("Time elapsed to receive UDP packet data from copter: "+(System.currentTimeMillis()-t)+"ms");
				String copterData = new String(rxbuffer,0,q.getLength());  
				System.out.println(copterData);
				copterDataList.add(copterData);
				
			}
			catch(Exception x)
			{
				 System.out.println(x); 
				 break;
			}
			
			t=System.currentTimeMillis();					
		}
		
		//Extract data: Motor Power, Altitude, Temperature, Pressure
		 int x,copterDataLength=copterDataList.size();
		 float y;
		 int[] motorData = new int[copterDataLength],altitudeData=new int[copterDataLength];
		 float[] tempData = new float[copterDataLength],pressureData=new float[copterDataLength];
		 
		 //Parse copter data 
		 for(int i=0; i<copterDataList.size();i++)
		 {
			 x=Integer.parseInt(copterDataList.get(i).substring(40,43));
			 System.out.println(x);
			 x=Integer.parseInt(copterDataList.get(i).substring(51,54));
			 System.out.println(x);
			 motorData[i]=x;
			 x=Integer.parseInt(copterDataList.get(i).substring(64,67));
			 System.out.println(x);
			 altitudeData[i]=x;
			 y=Float.parseFloat(copterDataList.get(i).substring(81,86));
			 System.out.println(y);
			 tempData[i]=y;
			 y=Float.parseFloat(copterDataList.get(i).substring(96,102));
			 System.out.println(y);
			 pressureData[i]=y;
		 }
		r_copter.close(); //Close Socket after receiving data
		
		//Save data in text files
		textSave(motorData,"C:\\Users\\Desktop\\data\\Motor.txt");
		textSave(altitudeData,"C:\\Users\\Desktop\\data\\Altitude.txt");
		textSave2(tempData,"C:\\Users\\Desktop\\data\\Temperature.txt");
		textSave2(pressureData,"C:\\Users\\Desktop\\data\\Pressure.txt");
	}
	public static ArrayList<String> vehicleData(InetAddress hostAddress,long duration) throws IOException
	{
		int portNumber = 29078;
		Socket server = new Socket(hostAddress,portNumber);
		server.setSoTimeout(2000);
		InputStream in = server.getInputStream();
		OutputStream out = server.getOutputStream();
		ArrayList<String> vehicleDataList = new ArrayList<String>(); //collect all response-data from server 
		String message = "01 1F\r"+"01 0F\r"+"01 11\r"+"01 0C\r"+"01 0D\r"+"01 05\r";
		long end, timeElapsed,start=System.currentTimeMillis();
		
		//take measurements for time interval specified by duration
		do
		{
			out.write(message.getBytes());
			int i;  
			char c; //variables i and c are help variables.
			int sourceData=0; //sourceData represents the id of the source data (engine run time, intake Air temp, ..)
			String vehicleData = new String("");
			
			for(;;)
			{
				try 
				{
				   i=in.read();
				   c=(char)i;
				   System.out.print(c);
				   if(i==13)
				   {
					 vehicleDataList.add(vehicleData);
					 vehicleData="";
					 sourceData++;
					 if(sourceData==6) break; 
				   }
				   else
				   {
					   vehicleData+=c;
				   }
				}
				catch(Exception e)
				{
					System.out.println(e);
					break;
				}
				
			}
			end=System.currentTimeMillis();
			timeElapsed=end-start;
		}while(timeElapsed<=duration);
		
		out.close();
		in.close();
		server.close();
		
		return vehicleDataList;
	}
	public static void vehicleRecord(ArrayList<String> vehicleDataList) throws IOException
	{
		int id,x,y;
		int engineRunTime,intakeTemp,throttlePos,engineRPM,vehicleSpeed,coolantTemp;
		int dataLength=vehicleDataList.size()/6; //data length for each parametre (6 parametres) is the 1/6 of all data
		String firstByte,secondByte;
		System.out.println();
		
		//initialize arrays where the data will be kept
		int[] array0 = new int[dataLength], array1 = new int[dataLength] , array2 = new int[dataLength];
		int[] array3 = new int[dataLength], array4 = new int[dataLength] , array5 = new int[dataLength];
		
		for(int i=0; i<vehicleDataList.size(); i++)
		{
			System.out.println(vehicleDataList.get(i));
			id=i%6;
			System.out.println("ID IS "+i%6);
			//Depending on the value of i, we have a specific id that determines which parametre data we have to parse and save.
			switch(id)
			{
				//Engine Run Time
				case(0): 
					firstByte=vehicleDataList.get(i).substring(6,8);
					System.out.println(firstByte);
					x = Integer.parseInt(firstByte, 16);					
					System.out.println("Hex value is " + x);
					secondByte=vehicleDataList.get(i).substring(9,11);
					System.out.println(secondByte);
					y = Integer.parseInt(secondByte, 16);					
					System.out.println("Hex value is " + y);
					engineRunTime=256*x+y;
					array0[i/6]=engineRunTime;
					break;
				//Intake air Temperature
				case(1):
					firstByte=vehicleDataList.get(i).substring(6,8);
					System.out.println(firstByte);
					x = Integer.parseInt(firstByte, 16);					
					System.out.println("Hex value is " + x);
					intakeTemp=x-40;
					array1[i/6]=intakeTemp;
					break;
				//Throttle Position
				case(2):
					firstByte=vehicleDataList.get(i).substring(6,8);
					System.out.println(firstByte);
					x = Integer.parseInt(firstByte, 16);					
					System.out.println("Hex value is " + x);
					throttlePos=Math.round((x*100)/255); //isws  kai kana typecast
					array2[i/6]=throttlePos;
					break;
				//Engine RPM
				case(3):
					firstByte=vehicleDataList.get(i).substring(6,8);
					System.out.println(firstByte);
					x = Integer.parseInt(firstByte, 16);					
					System.out.println("Hex value is " + x);
					secondByte=vehicleDataList.get(i).substring(9,11);
					System.out.println(secondByte);
					y = Integer.parseInt(secondByte, 16);					
					System.out.println("Hex value is " + y);
					engineRPM=Math.round(((x*256)+y)/4); 
					array3[i/6]=engineRPM;
					break;
				//Vehicle Speed
				case(4):
					firstByte=vehicleDataList.get(i).substring(6,8);
					System.out.println(firstByte);
					x = Integer.parseInt(firstByte, 16);					
					System.out.println("Hex value is " + x);
					vehicleSpeed=x;
					array4[i/6]=vehicleSpeed;
					break;
				//Coolant Temperature
				case(5):
					firstByte=vehicleDataList.get(i).substring(6,8);
					System.out.println(firstByte);
					x = Integer.parseInt(firstByte, 16);					
					System.out.println("Hex value is " + x);	
					coolantTemp=x-40;
					array5[i/6]=coolantTemp;
					break;
			}
			
		}
		//Save data
		textSave(array0,"C:\\Users\\Desktop\\data\\EngineRunTime.txt");
		textSave(array1,"C:\\Users\\Desktop\\data\\IntakeAirTemp.txt");
		textSave(array2,"C:\\Users\\Desktop\\data\\ThrottlePos.txt");
		textSave(array3,"C:\\Users\\Desktop\\data\\EngineRPM.txt");
		textSave(array4,"C:\\Users\\Desktop\\data\\VehicleSpeed.txt");
		textSave(array5,"C:\\Users\\Desktop\\data\\CoolantTemp.txt");
	}
	public static void tcpHeaders(byte[] serverIP, int portNumber, String message) throws IOException
	{
		
		InetAddress hostAddress = InetAddress.getByAddress(serverIP);
		Socket server = new Socket(hostAddress,portNumber);
		InputStream in = server.getInputStream();
		OutputStream out = server.getOutputStream();

		out.write(message.getBytes());
		int i;
		char c;
		while((i=in.read())!=-1)
		{
			try 
			{
			   c=(char) i;
			   System.out.print(c);
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
		
		server.close();
		out.close();
		in.close();

	}
	public static void main(String[] args) throws IOException, LineUnavailableException {
		
		DatagramSocket s = new DatagramSocket();  //socket for send		 
		InetAddress hostAddress = InetAddress.getByAddress(hostIP); 
		s.connect(hostAddress, serverPort);
		DatagramSocket r = new DatagramSocket(clientPort); 
		InetAddress clientAddress = InetAddress.getByAddress(clientIP); 
		r.setSoTimeout(3500); 

		
		//**** ECHO REQUEST **** DELAY ON ****//
		
		ArrayList<ArrayList<Integer>> totalD= new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> responseTimeD=new ArrayList<Integer>();  
		ArrayList<Integer> movingAverageD= new ArrayList<Integer>();
		
		totalD=echoPacket(s,r,hostAddress,4*60*1000,1); //1 gia delay . 0 xwris delay
		responseTimeD=totalD.get(0);
		movingAverageD=totalD.get(1);
		
		int [] responseTimeArrayD = new int[responseTimeD.size()] , movingAverageArrayD=new int[movingAverageD.size()];
		responseTimeArrayD=toIntArray(responseTimeD);
		movingAverageArrayD=toIntArray(movingAverageD);
		textSave(responseTimeArrayD,"C:\\Users\\Desktop\\data\\ResponseTimeDelayON.txt");
		textSave(movingAverageArrayD,"C:\\Users\\Desktop\\data\\MovingAverageArrayDelayON.txt");
		
	
		//------------------------------------------------------------------------------------------------------//
		
		//**** ECHO REQUEST **** DELAY OFF ****//
		
		ArrayList<ArrayList<Integer>> total= new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> responseTime=new ArrayList<Integer>();  
		ArrayList<Integer> movingAverage= new ArrayList<Integer>();
		
		total=echoPacket(s,r,hostAddress,4*60*1000,0); //1 gia delay . 0 xwris delay
		responseTime=total.get(0);
		movingAverage=total.get(1);
		
		int [] responseTimeArray = new int[responseTime.size()] , movingAverageArray=new int[movingAverage.size()];
		responseTimeArray=toIntArray(responseTime);
		movingAverageArray=toIntArray(movingAverage);
		textSave(responseTimeArray,"C:\\Users\\Desktop\\data\\ResponseTimeDelayOFF.txt");
		textSave(movingAverageArray,"C:\\Users\\Desktop\\data\\MovingAverageArrayDelayOFF.txt");
		
		//------------------------------------------------------------------------------------------------------//
		
		//**** TEMPERATURE DATA FROM T00 ****//
		
		ArrayList<Integer> temperatureList= new ArrayList<Integer>();
		temperatureList=echoTemp(s,r,hostAddress);
		int [] temperatureArray = new int[temperatureList.size()] ;
		temperatureArray=toIntArray(temperatureList);
		textSave(temperatureArray,"C:\\Users\\Desktop\\data\\TemperatureValues.txt");
		
		//------------------------------------------------------------------------------------------------------//
		
		//**** IMAGE REQUEST AND LIVE VIDEO STREAMING ****//		

		byte[] imageArray = imageDataDownload(s,r,hostAddress,image);
		imageArray = imageExtract(imageArray);
		imageSave(imageArray,"photoFIX");
		
		imageArray = imageDataDownload(s,r,hostAddress,image2);
		imageArray = imageExtract(imageArray);
		imageSave(imageArray,"photoPTZ");	

		videoStream(s,r,hostAddress,6*60*1000);
						
		//------------------------------------------------------------------------------------------------------//
		
		//**** AUDIO REQUEST **** DPCM **** FREQ **** AQDPCM ****//

		byte[][] audioData128 = audioDataDownload128(s,r,hostAddress,sound128);
		byte[] audioBufferOutDPCM = audioDPCM(audioData128);
		audioSave(audioBufferOutDPCM,"fileDPCM1",8);
	    audioPlay(audioBufferOutDPCM,8000,8);
		
		audioData128 = audioDataDownload128(s,r,hostAddress,sound128freq);
		audioBufferOutDPCM = audioDPCM(audioData128);
		audioSave(audioBufferOutDPCM,"fileDCPMFREQ1",8);
	    audioPlay(audioBufferOutDPCM,8000,8);
	    
	    byte[][] audioData132 = audioDataDownload132(s,r,hostAddress,sound132);
		byte[] audioBufferOutAQDPCM = audioAQDPCM(audioData132);
		audioSave(audioBufferOutAQDPCM,"fileAQDPCM1",16);
		audioPlay(audioBufferOutAQDPCM,8000,16);
		
				
		//------------------------------------------------------------------------------------------------------//
		
		//**** TCP PROTOCOL ****//
		
    	tcpHeaders(hostIP,80,"GET /index.html HTTP/1.0\r\n\r\n");
		tcpHeaders(ifaIP,80,"GET http://www.ifa.gr/el/ HTTP/1.0\r\n\r\n");
		tcpHeaders(fordIP,80,"GET http://www.ford.gr/ HTTP/1.0\r\n\r\n");
		tcpHeaders(transitionSportsIP,80,"GET http://transitionsports.gr/index.php?lang=el HTTP/1.0\r\n\r\n");
		tcpHeaders(gazzetaIP,80,"GET http://www.gazzetta.gr/basketball/euroleague/omada/olympiacos HTTP/1.1\r\n\r\n");
		tcpHeaders(meteoIP,80,"GET http://meteo.gr/ HTTP/1.0\r\n\r\n");
		tcpHeaders(ethmmyIP,8083,"GET http://alexander.ee.auth.gr:8083/eTHMMY/cms.course.login.do?method=execute&PRMID=56 HTTP/1.1\r\n\r\n");
		tcpHeaders(artemisIP,80,"GET http://155.207.26.179/status.php HTTP/1.0\r\n\r\n");

		//------------------------------------------------------------------------------------------------------//
		
		//**** UDP PROTOCOL **** COPTER ****//

		copterDataTelemetry();
		
		//------------------------------------------------------------------------------------------------------//
		
		//**** TCP PROTOCOL **** OBD-II Vehicle **** 
			
		ArrayList<String> vehicleDataList = vehicleData(hostAddress,6*60*1000);
		vehicleRecord(vehicleDataList);
		
		//------------------------------------------------------------------------------------------------------//
		
		System.out.println("End");
		s.disconnect();
		s.close();
		r.disconnect();
		r.close();
		System.exit(0);
		
	}

}
