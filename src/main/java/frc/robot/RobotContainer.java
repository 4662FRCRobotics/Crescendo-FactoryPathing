// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.PS4Controller.Button;
import frc.robot.Constants.OIConstants;
import frc.robot.libraries.ConsoleAuto;
import frc.robot.subsystems.AutonomousSubsystem;
import frc.robot.subsystems.DriveSubsystem;
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
    m_driverController.x()
        .whileTrue(new RunCommand(
            () -> m_robotDrive.setX(),
            m_robotDrive));

    runAutoConsoleFalse();
    //new Trigger(DriverStation::isDisabled)
    //new Trigger(RobotModeTriggers.disabled())
    new Trigger(trgAutoSelect())
      .whileTrue(m_autonomousSubysystem.cmdAutoSelect());
    runAutoConsoleTrue();

    //THIS DOES NOT WORK //
    new Trigger(RobotModeTriggers.disabled())
      .onTrue(Commands.runOnce(this::runAutoConsoleTrue))
      ;
    // WHY NOT??????

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

  public Command getDrivePathCommand(DriveSubsystem.DrivePaths pathName) {
    return m_robotDrive.getPathStep(pathName);
  }
}
