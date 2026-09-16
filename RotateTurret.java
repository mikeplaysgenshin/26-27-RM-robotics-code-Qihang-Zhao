import com.qualcomm.robotcore.hardware.Servo;
public class RotateTurret{
  public DcMotorEx RotatingTurret;
  public double currentHeading=0;
  public double degreePerSecond;
  public int ticksPerSecond;
  //sensitivity=desired degreePerSecond/ticks/degreePerSecond at full power
  public double sensitivity;
  public double power;
  public int resetRotation;
  public RotateTurret(HardwareMap hardwareMap) {
    RotatingTurret = hardwareMap.get(DcMotorEx.class, "RotatingTurret");
  }
  public init(){
    currentHeading=0;
  }
    //autonoumus  
    public void AutoRotate(Double degrees){
      
      currentHeading+=degrees;
      //reset
      if(currentHeading>-360){
        currentHeading-=360;
      }
      //prevent rotating too much by setting boundary at 190/170 degrees
      if(degrees>0&&currentHeading>190){
        degrees-=360;
        resetrotation=1;
      }else if(degrees<0&&currentHeading<170){
        degrees+=360;
        resetRotation=1;
      }
      if(resetRotation==0){
      if(Double degrees>0){
      RotatingTurret.setPower(1);
        //letting the DcmotorMove
        sleep(Math.abs(degrees)/degreePerSecond*1000);
        RotatingTurret.setPower(0);
    }else{
        RotatingTurret.setPower(-1);
        sleep(Math.abs(degrees)/degreePerSecond*1000);
        RotatingTurret.setPower(0);
      }
      }else{
       if(Double degrees>0){
         RotatingTurret.setPower(1);
       }else{
       }
      }
    }
  //teleOp
      public void TeleRotate(float magnitude,int direction){
        if(currentHeading>-360){
        currentHeading-=360;
      }
        power=magnitude*sensitivity*direction;
        RotatingTurret.setPower(magnitude*sensitivity*direction);
        //update heading
        currentheading+=ticksPerSecond*power*degreePerSecond;
        //if exceeds target of 190/170,rotate back
        if(currentheading>190&&)
      }
}
