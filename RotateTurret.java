import java.util.Timer;
import java.util.TimerTask;
//import com.qualcomm.robotcore.hardware.Servo;
/**
 * Calculates and tracks turret motion. Angles are in degrees, speeds are in
 * degrees per second, accelerations are in degrees per second squared, and
 * durations passed to this class are in seconds.
 *
 * <p>This class currently has no FTC motor connected. The motion methods update
 * the requested power and state; hardware measurement methods fail explicitly
 * until a motor/encoder source is supplied.</p>
 */
public class RotateTurret {
  public double motorAcceleration;
  public double frictionDeceleration;
  public volatile double currentHeading;
  public double degreePerSecond;
  public int ticksPerSecond;
  public double sensitivity = 1.0;
  public volatile double power;
  public volatile boolean resetRotation;
  public volatile boolean isRotating;
  /** Measured acceleration speeds in degrees per second, one sample per tick. */
  public double[] speedList;
  public double maxPower = 1.0;
  /** Full-speed stopping duration, in seconds. */
  public double timeStop;
  double maxVelocity;
  //DcMotorEx RotatingTurret;  
  private Timer activeTimer;
  private long motionGeneration;

  public RotateTurret(double acceleration, double deceleration, int sampleRate,
      double maximumDegreesPerSecond) {
    requirePositiveFinite(acceleration, "acceleration");
    requirePositiveFinite(deceleration, "deceleration");
    requirePositiveFinite(maximumDegreesPerSecond, "maximumDegreesPerSecond");
    if (sampleRate <= 0) {
      throw new IllegalArgumentException("sampleRate must be greater than zero");
    }
    //RotatingTurret=hardwareMap.get(DcMotorEx.class,"RotatingTurret")
    motorAcceleration = acceleration;
    frictionDeceleration = deceleration;
    ticksPerSecond = sampleRate;
    degreePerSecond = maximumDegreesPerSecond;
    refreshDerivedValues();
  }

  /**
   * Returns [time until braking begins, peak motor power] for a move from rest.
   */
  public double[] MaxSafeSetting(double degreesRotation) {
    requireFinite(degreesRotation, "degreesRotation");
    refreshDerivedValues();
    double distance = Math.abs(degreesRotation);
    if (distance == 0.0) {
      return new double[] {0.0, 0.0};
    }

    double accelerationDistance = maxVelocity * maxVelocity
        / (2.0 * motorAcceleration);
    double brakingDistance = maxVelocity * maxVelocity
        / (2.0 * frictionDeceleration);
    double fullSpeedPower = maxPower;

    if (distance >= accelerationDistance + brakingDistance) {
      double accelerationTime = maxVelocity / motorAcceleration;
      double cruiseTime = (distance - accelerationDistance - brakingDistance)
          / maxVelocity;
      return new double[] {accelerationTime + cruiseTime, fullSpeedPower};
    }

    // For a short move, solve for the peak speed that fits both ramps:
    // d = v^2/(2a) + v^2/(2d).
    double peakVelocity = motorAcceleration*distance*Math.sqrt(motorAcceleration+motorAcceleration*motorAcceleration/frictionDeceleration);
    double accelerationTime = peakVelocity / motorAcceleration;
    double peakPower = Math.min(maxPower, peakVelocity / degreePerSecond);
    return new double[] {accelerationTime, peakPower};
  }

  /**
   * Uses {@link #speedList} as a measured, nondecreasing acceleration profile.
   * The result has the same meaning as {@link #MaxSafeSetting(double)}.
   */
  public double[] MaxSafeSettingNonLinear(double degreesRotation) {
    requireFinite(degreesRotation, "degreesRotation");
    refreshDerivedValues();
    if (speedList == null || speedList.length == 0) {
      throw new IllegalStateException("speedList must contain measured speed samples");
    }

    double distance = Math.abs(degreesRotation);
    if (distance == 0.0) {
      return new double[] {0.0, 0.0};
    }

    double sampleDuration = 1.0 / ticksPerSecond;
    double coveredDistance = 0.0;
    double previousSpeed = 0.0;
    double bestSpeed = 0.0;
    double bestTime = 0.0;
    double profilePeakSpeed = 0.0;
    double profilePeakTime = 0.0;
    double profilePeakDistance = 0.0;
    double previousSample = 0.0;

    for (int i = 0; i < speedList.length; i++) {
      double sample = speedList[i];
      requireFinite(sample, "speedList[" + i + "]");
      if (sample < 0.0) {
        throw new IllegalArgumentException("speedList values cannot be negative");
      }
      if (sample + 1e-9 < previousSample) {
        throw new IllegalArgumentException(
            "speedList must be nondecreasing during acceleration");
      }

      double speed = Math.min(sample, maxVelocity);
      coveredDistance += (previousSpeed + speed) * 0.5 * sampleDuration;
      double elapsed = (i + 1.0) * sampleDuration;
      if (speed > profilePeakSpeed) {
        profilePeakSpeed = speed;
        profilePeakTime = elapsed;
        profilePeakDistance = coveredDistance;
      }

      double distanceIncludingBraking = coveredDistance
          + speed * speed / (2.0 * frictionDeceleration);
      if (distanceIncludingBraking <= distance) {
        bestSpeed = speed;
        bestTime = elapsed;
      }
      previousSpeed = speed;
      previousSample = sample;
    }

    if (bestSpeed == 0.0) {
      // The requested move is shorter than the first measured profile step.
      return MaxSafeSetting(distance);
    }

    double fullProfileDistance = profilePeakDistance
        + profilePeakSpeed * profilePeakSpeed / (2.0 * frictionDeceleration);
    if (distance >= fullProfileDistance && profilePeakSpeed > 0.0) {
      double cruiseTime = (distance - fullProfileDistance) / profilePeakSpeed;
      return new double[] {
          profilePeakTime + cruiseTime,
          Math.min(maxPower, profilePeakSpeed / degreePerSecond)
      };
    }

    return new double[] {
        bestTime,
        Math.min(maxPower, bestSpeed / degreePerSecond)
    };
  }

  /**
   * Requests a relative autonomous move. The heading is normalized to [0, 360).
   */
  public void AutoRotate(double degrees) {
    requireFinite(degrees, "degrees");
    refreshDerivedValues();

    double targetHeading = normalizeHeading(currentHeading + degrees);
    double travelDegrees = degrees;
    boolean needsReset = false;
    if (degrees > 0.0 && targetHeading > 190.0) {
      travelDegrees -= 360.0;
      needsReset = true;
    } else if (degrees < 0.0 && targetHeading < 170.0) {
      travelDegrees += 360.0;
      needsReset = true;
    }

    if (travelDegrees == 0.0) {
      cancelActiveMotion();
      currentHeading = targetHeading;
      power = 0.0;
      isRotating = false;
      resetRotation = false;
      return;
    }

    double[] setting = MaxSafeSetting(Math.abs(travelDegrees));
    double requestedPower = Math.copySign(setting[1], travelDegrees);
    double brakingTime = Math.abs(requestedPower) * degreePerSecond
        / frictionDeceleration;
    validateMotionSchedule(setting[0], brakingTime);
    //RotatingTurret.setPower(requestedPower);
    cancelActiveMotion();
    currentHeading = targetHeading;
    resetRotation = needsReset;
    power = requestedPower;
    isRotating = true;
    scheduleMotionStop(setting[0], brakingTime);
  }

  /**
   * Requests a timed tele-operated move. The heading estimate uses the actual
   * requested duration, not the encoder sampling frequency.
   */
  public void TeleRotate(float magnitude, int direction, double time) {
    requireFinite(magnitude, "magnitude");
    requireFinite(time, "time");
    if (time < 0.0) {
      throw new IllegalArgumentException("time cannot be negative");
    }
    if (direction < -1 || direction > 1) {
      throw new IllegalArgumentException("direction must be -1, 0, or 1");
    }
    requireFinite(sensitivity, "sensitivity");
    refreshDerivedValues();

    double requestedPower = magnitude * sensitivity * direction;
    double commandedPower = Math.max(-maxPower,
        Math.min(maxPower, requestedPower));
    double proposedHeading = currentHeading
        + commandedPower * degreePerSecond * time;
    double targetHeading = normalizeHeading(proposedHeading);

    boolean needsReset = (targetHeading > 190.0 && direction == 1)
        || (targetHeading < 170.0 && direction == -1);
    double movementPower;
    double driveTime;
    double brakingTime;
    if (needsReset) {
      double[] fullTurn = MaxSafeSetting(360.0);
      double correctionDirection = direction == 1 ? -1.0 : 1.0;
      movementPower = correctionDirection * fullTurn[1];
      driveTime = fullTurn[0];
      brakingTime = Math.abs(movementPower) * degreePerSecond
          / frictionDeceleration;
    } else {
      movementPower = commandedPower;
      driveTime = time;
      brakingTime = Math.abs(movementPower) * degreePerSecond
          / frictionDeceleration;
    }
    validateMotionSchedule(driveTime, brakingTime);
    //RotatingTurret.setPower(movementPower);
    cancelActiveMotion();
    currentHeading = targetHeading;
    resetRotation = needsReset;
    power = movementPower;
    isRotating = Math.abs(power) > 0.0 && driveTime > 0.0;
    scheduleMotionStop(driveTime, brakingTime);
  }

  /** Hardware-dependent methods are deliberately explicit until motor I/O exists. */
  public double[] motorTestaccGraph() {
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

  public double[] motorTestspeedGraph() {
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

  public double[] motorTestTotalDistance() {
    double rounds = 0;
    double currentPosition = 0;
    double lastPosition = 0;
    boolean first = true;
    long startTime = System.nanoTime();
    double startAngle =0;
    // startAngle = RotatingTurret.getPosition();
    //RotatingTurret.setPower(0.8);
    double[] returnList = new double[2];
    double speed=0;
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
    double totalangle = rounds * 360 + currentPosition-startAngle;
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

  public double[] motorTestLinear() {
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

  public double GetHeading() {
    return currentHeading;
  }

  public boolean GetresetRotation() {
    return resetRotation;
  }

  public boolean GetIsRotating() {
    return isRotating;
  }

  private synchronized void scheduleMotionStop(double driveTimeSeconds,
      double brakingTimeSeconds) {
    long powerOffDelay = secondsToMillis(driveTimeSeconds);
    long completionDelay = secondsToMillis(driveTimeSeconds + brakingTimeSeconds);
    if (activeTimer != null) {
      activeTimer.cancel();
    }
    long generation = motionGeneration;
    Timer timer = new Timer("turret-motion", true);
    activeTimer = timer;

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        synchronized (RotateTurret.this) {
          if (generation == motionGeneration) {
            power = 0.0;
            //RotatingTurret.setPower(0);
          }
        }
      }
    }, powerOffDelay);

    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        synchronized (RotateTurret.this) {
          if (generation == motionGeneration) {
            power = 0.0;
             //RotatingTurret.setPower(0);
            isRotating = false;
            resetRotation = false;
            activeTimer = null;
          }
          timer.cancel();
        }
      }
    }, completionDelay);
  }

  private static void validateMotionSchedule(double driveTimeSeconds,
      double brakingTimeSeconds) {
    secondsToMillis(driveTimeSeconds);
    secondsToMillis(driveTimeSeconds + brakingTimeSeconds);
  }

  private synchronized void cancelActiveMotion() {
    motionGeneration++;
    if (activeTimer != null) {
      activeTimer.cancel();
      activeTimer = null;
    }
  }

  private void refreshDerivedValues() {
    requirePositiveFinite(motorAcceleration, "motorAcceleration");
    requirePositiveFinite(frictionDeceleration, "frictionDeceleration");
    requirePositiveFinite(degreePerSecond, "degreePerSecond");
    if (ticksPerSecond <= 0) {
      throw new IllegalStateException("ticksPerSecond must be greater than zero");
    }
    requireFinite(maxPower, "maxPower");
    if (maxPower <= 0.0 || maxPower > 1.0) {
      throw new IllegalStateException("maxPower must be greater than 0 and at most 1");
    }
    maxVelocity = maxPower * degreePerSecond;
    timeStop = maxVelocity / frictionDeceleration;
  }

  private static long secondsToMillis(double seconds) {
    requireFinite(seconds, "duration");
    if (seconds < 0.0) {
      throw new IllegalArgumentException("duration cannot be negative");
    }
    double milliseconds = Math.ceil(seconds * 1000.0);
    // Leave room for Timer's absolute timestamp arithmetic to add the current
    // wall-clock time without overflowing a signed long.
    if (Double.isInfinite(milliseconds) || milliseconds > Long.MAX_VALUE / 4.0) {
      throw new IllegalArgumentException("duration is too large to schedule");
    }
    return (long) milliseconds;
  }

  private static double normalizeHeading(double heading) {
    requireFinite(heading, "heading");
    double normalized = heading % 360.0;
    return normalized < 0.0 ? normalized + 360.0 : normalized;
  }

  private static void requireFinite(double value, String name) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      throw new IllegalArgumentException(name + " must be finite");
    }
  }

  private static void requirePositiveFinite(double value, String name) {
    requireFinite(value, name);
    if (value <= 0.0) {
      throw new IllegalArgumentException(name + " must be greater than zero");
    }
  }

  private static UnsupportedOperationException hardwareUnavailable() {
    return new UnsupportedOperationException(
        "Motor measurement requires an attached motor and encoder source");
  }
}
