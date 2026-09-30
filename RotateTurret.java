import com.qualcomm.robotcore.hardware.Servo;
import java.util.Timer;
import java.util.TimerTask;
public class RotateTurret{
  public double motorAccleration;
  public double frictionDeceleration;//needs to be positive in degrees
  public DcMotorEx RotatingTurret;
  public double currentHeading=0;
  public double degreePerSecond;//rounds per second times 360 at full power
  public int ticksPerSecond;
  //sensitivity=desired degreePerSecond/ticks/degreePerSecond at full power
  public double sensitivity;
  public double power;
  public boolean resetRotation;
  public boolean isRotating;
  public double maxPower;//power motor(* degreePerSecond to get angular velocity)
  public double maxVelocity=maxPower*degreePerSecond/tickspersecond;
  public double[] SafeMaxPower(double degreesRotation){
    //change of x=velocity*time+half acceleration *time squared
    //vf=vi+at
    double timeStart=maxvelocity/motorAcceleration;
    double timeStop=maxvelocity/frictionDecelration;
    double maxStartingAngle=timeStart*timeStart*motorAcceleration;
    double maxSlideDegree=timeStop*maxspeed*degreePerSecond-0.5*frictionDeceleration*time*time;
    double[]returnList=new double[2];//1:timeacceleration,2:maxpower;
    double timeMotorRun;
    double timeCruise;
    if(degrees>=maxSlideDegree+maxStartingAngle){
      timeCruise=(degreeRotation-maxStartingAngle-maxSlidingAngle)/maxVelocity;
      timeMotorRun=timeCruise+timeStart;
      returnList={timeMotorRun,maxPower};
      return returnList;
    }else{
      double totaltime=Math.sqrt((3/2*motorAcceleration-1/2*frictiondecelration)*degreesRotation)/(3/2*motorAcceleration-1/2*frictiondecelration);
      timeMotorRun=totaltime-timeStop;
      double maxPowerInstance=totaltime*acceleration/degreePerSecond*ticksPersecond;
      returnList={timeMotorRun,maxPowerInstance};
      return returnList;
    }
  }
  public RotateTurret(HardwareMap hardwareMap) {
    RotatingTurret = hardwareMap.get(DcMotorEx.class, "RotatingTurret");
  }
  public init(){
    currentHeading=0;
  }
  }
  public double[] motorTestaccGrahph(){//uses derviative,returns instant acceleration graph
     int rounds=0;
    double currentPostition=0;
    double lastPostition=0;
    boolean first=true;
    long startTime = System.nanoTime();
    RotatingTurret.setPower(0.8);
    double[]speedList=new doouble[1000];
    double counter=0;
    while(speed<0.8*degreePerSecond){
      
      if(first){
        first=false;
      }else{
        lastPosition=currentPosition;
      }
      currentPosition=RotatingTurret.getPosition();
      if(currentPostion<lastPosition){
round+=1;
            speed=(currentPostion-lastPosition+360)/ticksPerSecond; 
      }else{
      speed=(currentPostion-lastPosition)/ticksPerSecond;
      }
      speedList[counter]=speed;
      counter++;
    }
    double[]accList=new double[1000];
    for(int i=1;i<counter;i++){
      accList=(speed[i]-speed[i-1])/ticksPerSecond;
    }
    return accList;

  }
    public double[] motorTestspeedGraph(){//uses derviative,returns instant acceleration graph
     int rounds=0;
    double currentPostition=0;
    double lastPostition=0;
    boolean first=true;
    long startTime = System.nanoTime();
    RotatingTurret.setPower(0.8);
    double[]speedList=new doouble[1000];
    double counter=0;
    while(speed<0.8*degreePerSecond){
      
      if(first){
        first=false;
      }else{
        lastPosition=currentPosition;
      }
      currentPosition=RotatingTurret.getPosition();
      if(currentPostion<lastPosition){
round+=1;
            speed=(currentPostion-lastPosition+360)/ticksPerSecond; 
      }else{
      speed=(currentPostion-lastPosition)/ticksPerSecond;
      }
      speedList[counter]=speed;
      counter++;
    }
      return speedList;
  }
public double[] motorTestTotalDistance(){//first acceleration,second friction
    int rounds=0;
    double currentPostition=0;
    double lastPostition=0;
    boolean first=true;
    long startTime = System.nanoTime();
    RotatingTurret.setPower(0.8);
    double[]returnList=new double[2];
    while(speed<0.8*degreePerSecond){
      
      if(first){
        first=false;
      }else{
        lastPosition=currentPosition;
      }
      currentPosition=RotatingTurret.getPosition();
      if(currentPostion<lastPosition){
round+=1;
            speed=(currentPostion-lastPosition+360)/ticksPerSecond; 
      }else{
      speed=(currentPostion-lastPosition)/ticksPerSecond;
      }
    }
    double totalangle=round*360+currentPosition;
    returnList[0]=totalangle;
    rotatingTurret.setPower(0);
    startTime = System.nanoTime();
     while(speed>0){
      
      if(first){
        first=false;
      }else{
        lastPosition=currentPosition;
      }
      currentPosition=RotatingTurret.getPosition();
      if(currentPostion<lastPosition){
round+=1;
         speed=(currentPostion-lastPosition+360)/ticksPerSecond;
      }else{
      speed=(currentPostion-lastPosition)/ticksPerSecond;
      }
    }
    endTime = System.nanoTime();
    durationsec = endTime - startTime;long startTime = System.nanoTime()/1000000000;
    totalAngle=round*360+currentPosition-startAngle;
  returnList[1]=totalangle;
    return ReturnAngle;
  }
  public double[] motorTestLinear(){//first acceleration,second friction
    int rounds=0;
    double currentPostition=0;
    double lastPostition=0;
    boolean first=true;
    long startTime = System.nanoTime();
    RotatingTurret.setPower(0.8);
    double[] returnList=new double[2];
    while(speed<0.8*degreePerSecond){
      
      if(first){
        first=false;
      }else{
        lastPosition=currentPosition;
      }
      currentPosition=RotatingTurret.getPosition();
      if(currentPostion<lastPosition){
round+=1;
            speed=(currentPostion-lastPosition+360)/ticksPerSecond; 
      }else{
      speed=(currentPostion-lastPosition)/ticksPerSecond;
      }
    }
    double totalangle=round*360+currentPosition;

long endTime = System.nanoTime();
long durationsec = endTime - startTime;long startTime = System.nanoTime()/1000000000;
    double startAngle=RotatingTurret.getPosition();
    double acceleration=2*totalangle/durationsec/durationsec;
    rotatingTurret.setPower(0);
    startTime = System.nanoTime();
     while(speed>0){
      
      if(first){
        first=false;
      }else{
        lastPosition=currentPosition;
      }
      currentPosition=RotatingTurret.getPosition();
      if(currentPostion<lastPosition){
round+=1; 
        speed=(currentPostion-lastPosition+360)/ticksPerSecond;
      }else{
      speed=(currentPostion-lastPosition)/ticksPerSecond;
      }
    }
    endTime = System.nanoTime();
    durationsec = endTime - startTime;long startTime = System.nanoTime()/1000000000;
    totalAngle=round*360+currentPosition-startAngle;
    double deceleration=2*totalangle-(0.8*degreesPerSecond)*durationsec/durationsec/durationsec;
    returnList[0]=acceleration;
    returnList[1]=deceleration;
    return returnList;
  }
    //autonomous  
    public void AutoRotate(Double degrees){
    rotating  
      currentHeading+=degrees;
      //reset
      if(currentHeading>360){
        currentHeading-=360;
      }
       if(currentHeading<0){
        currentHeading+=360;
      }
      //prevent rotating too much by setting boundary at 190/170 degrees
      if(degrees>0&&currentHeading>190){
        degrees-=360;
        resetrotation=true;
      }else if(degrees<0&&currentHeading<170){
        degrees+=360;
        resetRotation=true;
      }
      if(Double degrees>0){
        double[] getList=MaxSafePower(degrees);
        double power=getList[1];
        double time=getList[0];
         RotatingTurret.setPower(power);
                isRotating=true;
       timer.schedule(new TimerTask() {
       

        //letting the DcmotorMove
        RotatingTurret.setPower(0);
        isRotating=false;
       },time);
    }else{
        double[] getList=MaxSafePower(degrees);
        double power=getList[1]*-1;
        double time=getList[0];
        RotatingTurret.setPower(power);
        isRotating=true;
         timer.schedule(new TimerTask() {
        RotatingTurret.setPower(0);
        isRotating=false;
        },time);
      }
   resetRotation=false;
    }
  //teleOp
      public void TeleRotate(float magnitude,int direction){
       
        //update heading
        currentheading+=ticksPerSecond*power*degreePerSecond;
         if(currentHeading>360){
        currentHeading-=360;
      }
          if(currentHeading<0){
        currentHeading+=360;
      }
        power=magnitude*sensitivity*direction;
        //if exceeds target of 190 forward/170 backward,rotate back
        if(currentheading>190&&direction==1){
          resetRotation=true;
          RotatingTurret.setPower(-1);
          isRotating=true;
           timer.schedule(new TimerTask() {
          RotatingTurret.setPower(0);
          isRotating=false;
           },360/degreePerSecond*1000);
      }else if(currentheading<170&&direction==-1){
          resetRotation=true;
          RotatingTurret.setPower(1);
          isRotating=true;
         timer.schedule(new TimerTask() {
          RotatingTurret.setPower(0);
          isRotating=false;
         },360/degreePerSecond*1000);
        }else{
          RotatingTurret.setPower(magnitude*sensitivity*direction);
        }
        resetRotation=false;
}
        //acessors
        public double GetHeading(){
          return currentHeading;
        }
        public boolean GetresetRotation(){
          reutrn resetRotation;
        }
        public boolean GetIsRotating(){
          reutrn isRotating;
        }
}
