code is for 2026-2027 season RMHS robotics team
current boundary of forward rotation is 190 for forward and 170 for backward
motor speed and sensitivity still have to be set because no physical structure exists
 public void AutoRotate(Double degrees), rotate certain degrees in autonomous operation
 public void TeleRotate(float magnitude,int direction) rotate certain degrees in teleop operation based on magnitude and direction
        public double GetHeading() returns current heading;    
        public boolean GetresetRotation() returns reset rotation condtion
        public boolean GetIsRotating() returns whether the turret is rotating or not
