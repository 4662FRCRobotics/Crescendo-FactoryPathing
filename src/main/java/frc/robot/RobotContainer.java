// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.XboxController;
import frc.robot.Constants.OIConstants;
import frc.robot.libraries.ConsoleAuto;
import frc.robot.subsystems.AutonomousSubsystem;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.NoteIntakeSubsystem;
import frc.robot.subsystems.NoteShooterSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.RobotModeTriggers;
import edu.wpi.first.wpilibj2.command.button.Trigger;

/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems
  private final DriveSubsystem m_robotDrive = new DriveSubsystem();
  private final NoteShooterSubsystem m_noteShooterSubsystem = new NoteShooterSubsystem();
  private final NoteIntakeSubsystem m_noteIntakeSubsystem = new NoteIntakeSubsystem();



  // The driver's controller
  CommandXboxController m_driverController = new CommandXboxController(OIConstants.kDriverControllerPort);

  private final ConsoleAuto m_consoleAuto =
      new ConsoleAuto(OIConstants.kAUTONOMOUS_CONSOLE_PORT);

  private final AutonomousSubsystem m_autonomousSubysystem = new AutonomousSubsystem(m_consoleAuto, this);

  static boolean m_runAutoConsole;
  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    // Configure the button bindings
    configureButtonBindings();

    // Configure default commands
    // CLIMBER - stop motors

    // DRIVE - teleop xBox joysticks
    m_robotDrive.setDefaultCommand(
        // The left stick controls translation of the robot.
        // Turning is controlled by the X axis of the right stick.
        new RunCommand(
            () -> m_robotDrive.drive(
                -MathUtil.applyDeadband(m_driverController.getLeftY(), OIConstants.kDriveDeadband),
                -MathUtil.applyDeadband(m_driverController.getLeftX(), OIConstants.kDriveDeadband),
                -MathUtil.applyDeadband(m_driverController.getRightX(), OIConstants.kDriveDeadband),
                false, false),
            m_robotDrive));

    // SHOOTER - idle motors
   // m_noteShooterSubsystem.setDefaultCommand(m_noteShooterSubsystem.cmdShooterIdle());

    // INTAKE - idle motors
    //m_noteIntakeSubsystem.setDefaultCommand(m_noteIntakeSubsystem.cmdSpinnerStop());
        
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be
   * created by
   * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its
   * subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling
   * passing it to a
   * {@link JoystickButton}.
   */
  private void configureButtonBindings() {
    //new JoystickButton(m_driverController, Button.kR1.value)

    // (X) DRIVE set X
    m_driverController.x()
        .whileTrue(new RunCommand(
            () -> m_robotDrive.setX(),
            m_robotDrive));

    // LEFT TRIGGER - Retract Intake and Shoot note
    m_driverController.leftTrigger(OIConstants.kCONTROLLER_TRIGGER_THRESHOLD)
        .whileTrue(cmdShootNote());

    // LEFT BUMPER intake note
    m_driverController.leftBumper()
      .whileTrue(cmdPickUpNote());
      
    // (A) Intake Retract
    m_driverController.a()
      .whileTrue(m_noteIntakeSubsystem.cmdRetractIntake());

    // (Y) while true intake note (spinner) and shooter reverse (human player)
    m_driverController.y()
      .whileTrue(Commands.sequence(m_noteIntakeSubsystem.cmdRetractIntake(),
        Commands.parallel(m_noteShooterSubsystem.cmdShooterIntake(),
        m_noteIntakeSubsystem.cmdSpinnerTopIntake())
      ));
    // (Y) on false holdNote and then stop shooter

    // (B) while true drop note on false hold note
    m_driverController.b()
      .whileTrue(m_noteIntakeSubsystem.cmdDropNote());

    runAutoConsoleFalse();
    //new Trigger(DriverStation::isDisabled)
    //new Trigger(RobotModeTriggers.disabled())
    new Trigger(trgAutoSelect())
      .whileTrue(m_autonomousSubysystem.cmdAutoSelect());
    runAutoConsoleTrue();

    new Trigger(RobotModeTriggers.disabled())
      .onTrue(Commands.runOnce(this::runAutoConsoleTrue)
        .ignoringDisable(true))
      ;

    new Trigger(RobotModeTriggers.disabled())
    .onFalse(Commands.runOnce(this::runAutoConsoleFalse))
    ;
  }

  
  private static Trigger trgAutoSelect() {
    //System.out.println("bool auto console" + m_runAutoConsole);
    return new Trigger(() -> m_runAutoConsole);
  }

  private void runAutoConsoleTrue() {
    m_runAutoConsole = true;
    System.out.println("true " + m_runAutoConsole);
  }

  private void runAutoConsoleFalse() {
    m_runAutoConsole = false;
    System.out.println("false " + m_runAutoConsole);
  }


  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return m_autonomousSubysystem.cmdAutoControl();
//    return null;
  }

  public Command getDrivePathCommand(String pathName) {
    return m_robotDrive.getPathStep(pathName);
  }
  
  public Command getIntakePathCommand(String pathName, double dWaitTime) {
    return m_robotDrive.getPathStep(pathName);
  }

  /*
   * Command to shoot the NOTE
   * Parallel command to spin up the Shooter
   * with Wait period to allow shooter to come to speed (could be a wait until on shooter RPM true)
   * followed by Intake feeding out the NOTE
   * 
   * Available as public so it can be used by Autonomous
   */
  public Command cmdShootNote() {
    return (Commands.sequence(
      m_noteIntakeSubsystem.cmdRetractIntake(),
      Commands.parallel(m_noteShooterSubsystem.cmdShooterLaunch(),
                Commands.sequence(Commands.waitSeconds(OIConstants.kINTAKE_FEED_DELAY),
                    m_noteIntakeSubsystem.cmdSpinnerEject()))
      )
    );
  }

  public Command cmdPickUpNote() {
    return m_noteIntakeSubsystem.cmdPickUpNote();
  }

}
