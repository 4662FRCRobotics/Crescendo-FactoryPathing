// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkLimitSwitch;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import frc.robot.Constants.IntakeConstants;

/*
 * The NoteIntakeSubsystem (simple) takes a pre-loaded NOTE and feeds the
 * NoteShooterSubsystem when commanded
 * The NOTE feed uses one (1) SparkMAX with a limit switch indicating 
 * presence of a NOTE.
 */

public class NoteIntakeSubsystem extends SubsystemBase {

  private CANSparkMax m_motorIntakeSpinner;
  private CANSparkMax m_motorIntakeLift;
  private SparkLimitSwitch m_isNoteLoaded;
  private SparkLimitSwitch m_isForwardLimit;
  private SparkLimitSwitch m_isReverseLimit;

  /** Creates a new NoteIntakeSubsystem. */
  public NoteIntakeSubsystem() {

    m_motorIntakeSpinner = new CANSparkMax(IntakeConstants.kINTAKE_SPIN_MOTOR_ID, MotorType.kBrushless);
    m_motorIntakeLift = new CANSparkMax(IntakeConstants.kINTAKE_LIFT_MOTOR_ID, MotorType.kBrushless);
    m_motorIntakeSpinner.setIdleMode(IdleMode.kBrake);
    m_motorIntakeLift.setIdleMode(IdleMode.kBrake);

    m_isNoteLoaded = m_motorIntakeSpinner.getForwardLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen);
    m_isForwardLimit = m_motorIntakeLift.getForwardLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen);
    m_isReverseLimit = m_motorIntakeLift.getReverseLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen);

  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putBoolean("Forward Limit Switch", m_isForwardLimit.isPressed());
    SmartDashboard.putBoolean("Reverse Limit Switch", m_isReverseLimit.isPressed());
    SmartDashboard.putBoolean("Note Captured", m_isNoteLoaded.isPressed());
  }

  private void setSpinnerSpeed(double spinSpeed) {
    m_motorIntakeSpinner.set(spinSpeed);
  }

  private void setIntakeSpeed(double intakeSpeed) {
    m_motorIntakeLift.set(intakeSpeed);
  }

  public boolean isNoteIn(){
    return m_isNoteLoaded.isPressed();
  }

  private boolean isIntakeExtended() {
    return m_isForwardLimit.isPressed();
  }

  private boolean isIntakeRetracted() {
    return m_isReverseLimit.isPressed();
  }

  public Command cmdSpinnerEject() {
    return Commands.runEnd(() -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_EJECT),
     () -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_STOP),
     this);
  }

  //public Command cmdSpinnerStop() {
  //  return Commands.run(() -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_STOP), this);
  //}

  public Command cmdSpinnerIntake() {
    return new FunctionalCommand(
      () -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_INTAKE),
      () -> {} ,
      (interrupted) -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_STOP),
      () -> isNoteIn(),
      this
    );
  }

  public Command cmdSpinnerTopIntake() {
    return new FunctionalCommand(
      () -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_TOP_INTAKE),
      () -> {} ,
      (interrupted) -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_STOP),
      () -> isNoteIn(),
      this
    );
  }

  public Command cmdDeployIntake() {
    return new FunctionalCommand(
      () -> setIntakeSpeed(IntakeConstants.kINTAKE_SPEED_DEPLOY),
      () -> {} ,
      (interrupted) -> setIntakeSpeed(IntakeConstants.kINTAKE_SPEED_STOP),
      () -> isIntakeExtended(),
      this
    );
  }

  public Command cmdRetractIntake() {
    return new FunctionalCommand(
      () -> setIntakeSpeed(IntakeConstants.kINTAKE_SPEED_RETRACT),
      () -> {} ,
      (interrupted) -> setIntakeSpeed(IntakeConstants.kINTAKE_SPEED_STOP),
      () -> isIntakeRetracted(),
      this
    );
  }

  /* cmdPickUpNote
   * command to pick up a note from the floor
   * combines deploy, spin, and retract into one set based on internal states
   * deploy - only if intake is empty and not extended
   * spin - while intake is empty
   * retract - just do it if you got this far
   */
  public Command cmdPickUpNote() {
    return cmdDeployIntake()
        .unless(() -> isNoteIn() | isIntakeExtended())
        .andThen(cmdSpinnerIntake()
          .unless(() -> isNoteIn()))
        .andThen(cmdRetractIntake())
    ;
  }

  public Command cmdDropNote() {
    return cmdDeployIntake()
      .unless(() -> isIntakeExtended())
      .andThen(cmdSpinnerEject())
    ;
  }
  
}
