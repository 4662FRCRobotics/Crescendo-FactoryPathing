// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.SparkLimitSwitch;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
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
  private SparkLimitSwitch m_isNoteLoaded;

  /** Creates a new NoteIntakeSubsystem. */
  public NoteIntakeSubsystem() {

    m_motorIntakeSpinner = new CANSparkMax(IntakeConstants.kINTAKE_SPIN_MOTOR_ID, MotorType.kBrushless);
    m_motorIntakeSpinner.setIdleMode(IdleMode.kBrake);

    m_isNoteLoaded = m_motorIntakeSpinner.getForwardLimitSwitch(SparkLimitSwitch.Type.kNormallyOpen);

  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

  private void setSpinnerSpeed(double spinSpeed) {
    m_motorIntakeSpinner.set(spinSpeed);
  }

  public Command cmdSpinnerEject() {
    return Commands.runEnd(() -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_EJECT),
     () -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_IDLE),
     this);
  }

  public Command cmdSpinnerIdle() {
    return Commands.run(() -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_IDLE), this);
  }

  public Command cmdSpinnerIntake() {
    return Commands.runEnd(() -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_INTAKE),
     () -> setSpinnerSpeed(IntakeConstants.kSPINNER_SPEED_IDLE),
      this);
  }
  
}
