//import com.qualcomm.robotcore.hardware.Servo;
import java.util.Timer;
import java.util.TimerTask;

public class RotateTurret {
  public double motorAcceleration;
  public double frictionDeceleration;// needs to be positive in degrees
  //public DcMotorEx RotatingTurret;
  public double currentHeading = 0;
  public double degreePerSecond;// rounds per second times 360 at full power
  public int ticksPerSecond;
  // sensitivity=desired degreePerSecond/ticks/degreePerSecond at full power
  public double sensitivity;
  public double power;
  public boolean resetRotation;
  public boolean isRotating;
  public double[] speedList;
  public double maxPower;
  public double timeStop;
   double maxVelocity = maxPower * degreePerSecond / ticksPerSecond;
  // change of x=velocity*time+half acceleration *time squared
  // vf=vi+at
  // temporary constructor
  public RotateTurret(Double acc, Double dcc, int ticks, Double degPS) {
    motorAcceleration = acc;
    frictionDeceleration = dcc;
    ticksPerSecond = ticks;
    degreePerSecond = degPS;
    timeStop = maxVelocity / frictionDeceleration;
  }


  public double[] MaxSafeSetting(double degreesRotation) {
    double timeStart = maxVelocity / motorAcceleration;
    double timeStop = maxVelocity / frictionDeceleration;
    double maxStartingAngle = timeStart * timeStart * motorAcceleration;
    double maxSlideDegree = timeStop * maxVelocity * degreePerSecond - 0.5 * frictionDeceleration * timeStop * timeStop;
    double[] returnList = new double[2];// 1:timeacceleration,2:maxpower;
    double timeMotorRun = timeStop + timeStart;
    if (degreesRotation >= maxSlideDegree + maxStartingAngle) {
      timeCruise = (degreesRotation - maxStartingAngle - maxSlideDegree) / maxVelocity;
      returnList[0] = timeMotorRun;
      returnList[1] = 1;
      return returnList;
    } else {
      double totaltime = Math.sqrt((3 / 2 * motorAcceleration - 1 / 2 * frictionDeceleration) * degreesRotation)
          / (3 / 2 * motorAcceleration - 1 / 2 * frictionDeceleration);
      timeMotorRun = totaltime - timeStop;
      double maxPowerInstance = totaltime * motorAcceleration / degreePerSecond * ticksPerSecond;
      returnList[0] = timeMotorRun;
      returnList[1] = maxPowerInstance;
      return returnList;
    }
  }

  public double[] MaxSafeSettingNonLinear(double degreesRotation) {
    double timeStart =0;
    double timeStop = maxVelocity / frictionDeceleration;
    double maxStartingAngle = 0;
    for (int i = 0; i < speedList.length; i++) {
      maxStartingAngle += speedList[i] / ticksPerSecond;
      timeStart+=1/ticksPerSecond;
    }
    double maxSlidingAngle = timeStop * maxspeed * degreePerSecond - 0.5 * frictionDeceleration * timeStop * timeStop;
    double[] returnList = new double[2];// 1:timeacceleration,2:maxpower;
    double timeMotorRun;
    double timeCruise;
    double totalDistanceAcc;
    if (degreesRotation >= maxSlidingAngle + maxStartingAngle) {
      timeCruise = (degreesRotation - maxStartingAngle - maxSlidingAngle) / maxVelocity;
      timeMotorRun = timeCruise + timeStart;
      returnList[0] = timeMotorRun;
      returnList[1] = maxPower;
      return returnList;
    } else {
      int counter = 0;
      double currentSpeed;
      while (speedList[counter]<maxVelocity) {
        totalDistanceAcc += speedList[counter] / ticksPerSecond;
        counter++;
        currentSpeed = speedList[counter];
      }

      double totaltime = counter / ticksPerSecond;
      returnList[1] = totaltime;
      returnList[0] = currentSpeed * ticksPerSecond / degreePerSecond;

      return returnList;
    }
  }

  // public RotateTurret(HardwareMap hardwareMap) {
  // RotatingTurret = hardwareMap.get(DcMotorEx.class, "RotatingTurret");
  // currentHeading=0;
  // }
  public double[] motorTestaccGraph() {// uses derviative,returns instant acceleration graph
    int rounds = 0;
    double currentPosition = 0;
    double lastPosition = 0;
    boolean first = true;
    long startTime = System.nanoTime();
    double speed;
    //RotatingTurret.setPower(0.8);
    double[] speedList = new double[1000];
    double counter = 0;
    while (speed <degreePerSecond) {

      if (first) {
        first = false;
      } else {
        lastPosition = currentPosition;
      }
      //currentPosition = RotatingTurret.getPosition();
      if (currentPosition < lastPosition) {
        rounds += 1;
        speed = (currentPostion - lastPosition + 360) / ticksPerSecond;
      } else {
        speed = (currentPosition - lastPosition) / ticksPerSecond;
      }
      speedList[counter] = speed;
      counter++;
    }
    double[] accList = new double[1000];
    for (int i = 1; i < counter; i++) {
      accList[i] = (speed[i] - speed[i - 1]) / ticksPerSecond;
    }
    return accList;

  }

  public double[] motorTestspeedGraph() {// uses derviative,returns instant acceleration graph
    double rounds = 0;
    double currentPosition = 0;
    double lastPosition = 0;
    boolean first = true;
    long startTime = System.nanoTime();
    //RotatingTurret.setPower(0.8);
    double[] speedList = new double[1000];
    double speed;
    double counter = 0;
    while (speed < 0.8 * degreePerSecond) {

      if (first) {
        first = false;
      } else {
        lastPosition = currentPosition;
      }
      //currentPosition = RotatingTurret.getPosition();
      if (currentPosition < lastPosition) {
        rounds += 1;
        speed = (currentPosition - lastPosition + 360) / ticksPerSecond;
      } else {
        speed = (currentPosition - lastPosition) / ticksPerSecond;
      }
      speedList[counter] = speed;
      counter++;
    }
    return speedList;
  }

  public double[] motorTestTotalDistance() {// first acceleration,second friction
    double rounds = 0;
    double currentPosition = 0;
    double lastPosition = 0;
    boolean first = true;
    long startTime = System.nanoTime();
    double startAngle =0;
    // startAngle = RotatingTurret.getPosition();
    //RotatingTurret.setPower(0.8);
    double[] returnList = new double[2];
    double speed;
    while (speed < 0.8 * degreePerSecond) {

      if (first) {
        first = false;
      } else {
        lastPosition = currentPosition;
      }
      //currentPosition = RotatingTurret.getPosition();
      if (currentPosition < lastPosition) {
        rounds += 1;
        speed = (currentPosition - lastPosition + 360) / ticksPerSecond;
      } else {
        speed = (currentPosition - lastPosition) / ticksPerSecond;
      }
    }
    double totalangle = rounds * 360 + currentPosition;
    returnList[0] = totalangle;
    //rotatingTurret.setPower(0);
    startTime = System.nanoTime();
    // startAngle = RotatingTurret.getPosition();
    while (speed > 0) {

      if (first) {
        first = false;
      } else {
        lastPosition = currentPosition;
      }
      //currentPosition = RotatingTurret.getPosition();
      if (currentPosition < lastPosition) {
        rounds += 1;
        speed = (currentPosition - lastPosition + 360) / ticksPerSecond;
      } else {
        speed = (currentPosition - lastPosition) / ticksPerSecond;
      }
    }
    long endTime = System.nanoTime();
    long durationsec = (endTime - startTime)/1000000000;
    double totalAngle = rounds * 360 + currentPosition;
    returnList[1] = totalangle;
    return returnList;
  }

  public double[] motorTestLinear() {// first acceleration,second friction
    double rounds = 0;
    double currentPosition = 0;
    double lastPosition = 0;
    boolean first = true;
    long startTime = System.nanoTime();
    double speed=0;
    double startAngle=0;
    //startAngle=RotatingTurret.getPosition();
    //RotatingTurret.setPower(0.8);
    double[] returnList = new double[2];
    while (speed < 0.8 * degreePerSecond) {

      if (first) {
        first = false;
      } else {
        lastPosition = currentPosition;
      }
      //currentPosition = RotatingTurret.getPosition();
      if (currentPosition < lastPosition) {
        rounds += 1;
        speed = (currentPosition - lastPosition + 360) / ticksPerSecond;
      } else {
        speed = (currentPosition - lastPosition) / ticksPerSecond;
      }
    }
    double totalangle = rounds * 360 + currentPosition;

    long endTime = System.nanoTime();
    long durationsec = endTime - startTime;
    startTime = System.nanoTime() / 1000000000;
    // startAngle = RotatingTurret.getPosition();
    double acceleration = 2 * totalangle / durationsec / durationsec;
    //rotatingTurret.setPower(0);
    startTime = System.nanoTime();
    while (speed > 0) {

      if (first) {
        first = false;
      } else {
        lastPosition = currentPosition;
      }
      //currentPosition = RotatingTurret.getPosition();
      if (currentPosition < lastPosition) {
        rounds += 1;
        speed = (currentPosition - lastPosition + 360) / ticksPerSecond;
      } else {
        speed = (currentPosition - lastPosition) / ticksPerSecond;
      }
    }
    endTime = System.nanoTime();
    durationsec = (endTime - startTime)/1000000000;
    
    totalAngle = rounds * 360 + currentPosition - startAngle;
    double deceleration =-2*(totalAngle-degreePerSecond*durationsec)/durationsec/durationsec;
    returnList[0] = acceleration;
    returnList[1] = deceleration;
    return returnList;
  }

  // autonomous
  public void AutoRotate(Double degrees) {
    boolean resetRotation;
    Timer timer = new Timer();
    TimerTask stopRotate = new TimerTask() {
      @Override
      public void run() {
        // RotatingTurret.setPower(0);
        System.out.println("turret power stopped");
      }
    };
    TimerTask motorStop = new TimerTask() {
      @Override
      public void run() {
        isRotating = false;
        System.out.println("turret position stopped");
        resetRotation = false;
      }
    };
    currentHeading += degrees;
    // reset
    if (currentHeading > 360) {
      currentHeading -= 360;
    }
    if (currentHeading < 0) {
      currentHeading += 360;
    }
    // prevent rotating too much by setting boundary at 190/170 degrees
    if (degrees > 0 && currentHeading > 190) {
      degrees -= 360;
      resetRotation = true;
    } else if (degrees < 0 && currentHeading < 170) {
      degrees += 360;
      resetRotation = true;
    }
    if (degrees > 0) {
      double[] getList = MaxSafeSetting(degrees);
      double power = getList[1];
      double time = getList[0];
      // RotatingTurret.setPower(power);
      System.out.println("Motor started rotating");
      isRotating = true;
      timer.schedule(stopRotate, time);
      timer.schedule(motorStop, timeStop);
    } else {
      double[] getList = MaxSafeSetting(degrees);
      double power = getList[1] * -1;
      double time = getList[0];
      // RotatingTurret.setPower(power);
      isRotating = true;
      System.out.println("Motor started rotating");
      timer.schedule(stopRotate, time);
      timer.schedule(motorStop, timeStop + time);
    }

  }

  // teleOp
  public void TeleRotate(float magnitude, int direction, double time) {
    Timer timer = new Timer();
    TimerTask stopRotate = new TimerTask() {
      @Override
      public void run() {
        // RotatingTurret.setPower(0);
        System.out.println("turret power stopped");
      }
    };
    TimerTask motorStop = new TimerTask() {
      @Override
      public void run() {
        isRotating = false;
        System.out.println("turret position stopped");
        resetRotation = false;
      }
    };
    double[] roundList = MaxSafeSetting(360);
    // update heading
    currentheading += ticksPerSecond * power * degreePerSecond;
    if (currentHeading > 360) {
      currentHeading -= 360;
    }
    if (currentHeading < 0) {
      currentHeading += 360;
    }
    power = magnitude * sensitivity * direction;
    // if exceeds target of 190 forward/170 backward,rotate back
    if (currentheading> 190 && direction == 1) {
      resetRotation = true;
      // RotatingTurret.setPower(-1);
      System.out.println("Motor started rotating");
      isRotating = true;
      timer.schedule(stopRotate, roundList[0]);
      timer.schedule(motorStop, roundList[0] + timeStop);
    } else if (currentheading < 170 && direction == -1) {
      resetRotation = true;
      // RotatingTurret.setPower(1);
      isRotating = true;
      System.out.println("Motor started rotating");
      timer.schedule(stopRotate, roundList[0]);
      timer.schedule(motorStop, roundList[0] + timeStop);
    } else {
      // RotatingTurret.setPower(magnitude*sensitivity*direction);
      timer.schedule(stopRotate, time);
      timer.schedule(new TimerTask() {
        @Override
        public void run() {
          isRotating = false;
        }
      }, timeStop);
    }
  }


  // acessors
  public double GetHeading() {
    return currentHeading;
  }

  public boolean GetresetRotation() {
    return resetRotation;
  }

  public boolean GetIsRotating() {
    return isRotating;
  }
}
