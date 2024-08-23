// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.ShooterConstants;

import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

/*
 * The NoteShooterSubsystem is a side-by-side flywheel launcher
 * Actuators - two motors (right and left) using TalonFX controllers
 * The motors themselves can therefore be either Falcon 500s (VEX) or Kraken (WCP)
 * This subsystem assumes that they are used as pairs - not two different makes combined
 */

public class NoteShooterSubsystem extends SubsystemBase {
  /** Creates a new ShooterSubsystem. */

  private TalonFX m_motorShooterLeft;
  private TalonFX m_motorShooterRight;

  // constructor method - executed once at subsystem construction/instantiation
  public NoteShooterSubsystem() {

    m_motorShooterLeft = new TalonFX(ShooterConstants.kSHOOTER_LEFT_MOTOR_ID);
    m_motorShooterRight = new TalonFX(ShooterConstants.kSHOOTER_RIGHT_MOTOR_ID);

    m_motorShooterLeft.setInverted(!ShooterConstants.kIS_SHOOTER_INVERTED);
    m_motorShooterRight.setInverted(ShooterConstants.kIS_SHOOTER_INVERTED);

    m_motorShooterLeft.setNeutralMode(NeutralModeValue.Coast);
    m_motorShooterRight.setNeutralMode(NeutralModeValue.Coast);
    
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }

  private void setShootSpeed(double shootSpeed) {
    m_motorShooterLeft.set(shootSpeed);
    m_motorShooterRight.set(shootSpeed);
  }

  public Command cmdShooterIdle() {
    return Commands.run(() -> setShootSpeed(ShooterConstants.kSHOOT_SPEED_IDLE), this);
  }

  public Command cmdShooterLaunch() {
    return Commands.runEnd(() -> setShootSpeed(ShooterConstants.kSHOOT_SPEED_LAUNCH),
     () -> setShootSpeed(ShooterConstants.kSHOOT_SPEED_IDLE),
      this);
  }

}
