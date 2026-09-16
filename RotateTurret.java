import com.qualcomm.robotcore.hardware.Servo;
public class RotateTurret{
  public DcMotorEx RotatingTurret;
  public double currentHeading=0;
  public double degreePerSecond;
  //sensitivity=desired degreePerSecond/ticks/degreePerSecond at full power
  public double sensitivity;
  public RotateTurret(HardwareMap hardwareMap) {
    RotatingTurret = hardwareMap.get(DcMotorEx.class, "RotatingTurret");
  }
  public init(){
    currentHeading=0;
  }
    //autonoumus  
    public void AutoRotate(Double degrees){
      currentHeading+=degrees;
      if(currentHeading>-360){
        currentHeading-=360;
      }
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
    }
  //teleOp
      public void TeleRotate(float magnitude,int direction){
        RotatingTurret.setPower(magnitude*sensitivity*direction);
      }
}
