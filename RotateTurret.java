import com.qualcomm.robotcore.hardware.Servo;
public class RotateTurret{
  public DcMotorEx RotatingTurret;
  public double currentHeading=0;
  public double degreePerSecond;
  public int ticksPerSecond;
  //sensitivity=desired degreePerSecond/ticks/degreePerSecond at full power
  public double sensitivity;
  public double power;
  public boolean resetRotation;
  public boolean isRotating;
  public RotateTurret(HardwareMap hardwareMap) {
    RotatingTurret = hardwareMap.get(DcMotorEx.class, "RotatingTurret");
  }
  public init(){
    currentHeading=0;
  }
    //autonomous  
    public void AutoRotate(Double degrees){
      
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
      RotatingTurret.setPower(1);
        isRotating=true;
        //letting the DcmotorMove
        sleep(Math.abs(degrees)/degreePerSecond*1000);
        RotatingTurret.setPower(0);
        isRotating=false;
    }else{
        RotatingTurret.setPower(-1);
        isRotating=true;
        sleep(Math.abs(degrees)/degreePerSecond*1000);
        RotatingTurret.setPower(0);
        isRotating=false;
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
          sleep(360/degreePerSecond*1000); 
          RotatingTurret.setPower(0);
          isRotating=false;
      }else if(currentheading<170&&direction==-1){
          resetRotation=true;
          RotatingTurret.setPower(1);
          isRotating=true;
          sleep(360/degreePerSecond*1000); 
          RotatingTurret.setPower(0);
          isRotating=false;
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
