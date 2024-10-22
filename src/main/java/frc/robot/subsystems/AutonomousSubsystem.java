package frc.robot.subsystems;

import java.util.Map;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotContainer;
import frc.robot.libraries.ConsoleAuto;
//import frc.robot.Libraries.StepState;

//There is a 95% chance that it will crash if you try to run auto so dont
// Something interesting I found was DriverStation.getMatchTime() It returns how much time is left, might be useful.

public class AutonomousSubsystem extends SubsystemBase{

  // limited by the rotary switch settings of 6 and POV max of 8.
  public enum AutonomousCommands {
    WALLDRIVE,
    SPEAKERCENTER,
    SPEAKERLEFT,
    SPEAKERRIGHT;

    public String getSelectName() {
        return this.toString();
    }

    public int getSelectIx() {
        return this.ordinal();
    }
  }

  public enum AutonomousSteps {
    WAIT1('W', 1.0),
    WAIT2('W', 2.0),
    WAITLOOP('W', 99.9),
    WALL_OUT('D', 0.0, 1),
    SHOOTNOTE('S', 0.0, 1),
    SpkrCntrOut1('D', 0.0, 2),
    SpkrCntrRtrn1('D', 0.0, 3),
    DRV_INTK_1('I', 0.5, 2),
    DRV_STRT_1('D', 0.0, 3, 2),
    DRV_BACK_1('D', 0.0, 4),
    DRV_INTK_2('I', 0.0, 5),
    DRV_STRT_2('D', 0.0, 6, 5),
    END('E')
    ;

    private final char m_stepStruct;
    private final double m_waitTime;
    private final int m_iSwATrue;
    private final int m_iSwBFalse;

    private AutonomousSteps(char cStepStruct, double dWaitTime, int iSwATrue, int iSwBFalse) {
      this.m_stepStruct = cStepStruct;
      this.m_waitTime = dWaitTime;
      this.m_iSwATrue = iSwATrue;
      this.m_iSwBFalse = iSwBFalse;
    }

    private AutonomousSteps(char cStepStruct, double dWaitTime, int iSwATrue) {
      this.m_stepStruct = cStepStruct;
      this.m_waitTime = dWaitTime;
      this.m_iSwATrue = iSwATrue;
      this.m_iSwBFalse = 0;
    }

    private AutonomousSteps(char cStepStruct, double dWaitTime) {
      this.m_stepStruct = cStepStruct;
      this.m_waitTime = dWaitTime;
      this.m_iSwATrue = 0;
      this.m_iSwBFalse = 0;
    }
    
    private AutonomousSteps(char cStepStruct) {
      this.m_stepStruct = cStepStruct;
      this.m_waitTime = 0;
      this.m_iSwATrue = 0;
      this.m_iSwBFalse = 0;
    }

    public char getStepStruc() {
      return m_stepStruct;
    }

    public double getWaitTIme() {
      return m_waitTime;
    }

    public int getASwitch() {
      return m_iSwATrue;
    }

    public int getBSwitch() {
      return m_iSwBFalse;
    }

  }

  private String kAUTO_TAB = "Autonomous";
  private int kSTEP_MAX = 12;

  ConsoleAuto m_ConsoleAuto;
  RobotContainer m_robotContainer;

  AutonomousCommands m_autoSelectCommand[] = AutonomousCommands.values();
  AutonomousCommands m_selectedCommand;

  private String m_strCommand;
  private int m_iWaitCount;
  private AutonomousSteps[] m_autoStep = new AutonomousSteps[kSTEP_MAX];
  private String[] m_strStepList = new String[kSTEP_MAX];
  private String[] m_strStepSwitch = new String[kSTEP_MAX];
  private boolean[] m_bStepSWList = new boolean[kSTEP_MAX];
  private int m_iCmdCount = 0;

  private ShuffleboardTab m_tab = Shuffleboard.getTab(kAUTO_TAB);

  private GenericEntry m_autoCmd = m_tab.add("Selected Pattern", "")
      .withPosition(2, 0)
      .withSize(2, 1)
      .getEntry();

  private GenericEntry m_iWaitLoop = m_tab.add("WaitLoop", 0)
      .withWidget(BuiltInWidgets.kDial)
      .withPosition(4, 0)
      .withSize(2, 2)
      .withProperties(Map.of("min", 0, "max", 5))
      .getEntry();

  private GenericEntry m_allianceColor = m_tab.add("Alliance", true)
      .withWidget(BuiltInWidgets.kBooleanBox)
      .withProperties(Map.of("colorWhenTrue", "Red", "colorWhenFalse", "Blue"))
      .withPosition(0, 0)
      .withSize(2, 2)
      .getEntry();

  private int m_iPatternSelect;

  private AutonomousSteps[][] m_cmdSteps;

  public AutonomousSubsystem(ConsoleAuto consoleAuto, RobotContainer robotContainer) {

    m_ConsoleAuto = consoleAuto;
    m_robotContainer = robotContainer;
    m_selectedCommand = m_autoSelectCommand[0];
    m_strCommand = m_selectedCommand.toString();
    m_iPatternSelect = 0;
    m_iWaitCount = 0;

    for (int iat = 0; iat < kSTEP_MAX; iat++) {
      initStepList(iat);
      fmtDisplay(iat);
    }
  
/*
 *  CRITICAL PIECE
 * This two dimensional array defines the steps for each selectable Auto pattern
 * First dimension is set by the ConsoleAuto selector switch (passed in via POV 0)
 * Second dimension is the sequence of the possible step(s) for the pattern
 */
    m_cmdSteps = new AutonomousSteps[][] {
      //WALLDRIVE
          {AutonomousSteps.WAITLOOP,
           AutonomousSteps.WALL_OUT},
      //SPEAKERCENTER
          {AutonomousSteps.WAITLOOP, 
            AutonomousSteps.SHOOTNOTE, 
            AutonomousSteps.SpkrCntrOut1, 
            AutonomousSteps.WAIT1,
            AutonomousSteps.SpkrCntrRtrn1
          },
      //SPEAKERLEFT
          {AutonomousSteps.WAITLOOP,
            AutonomousSteps.SHOOTNOTE
          },
      //SPEAKERRIGHT
          {AutonomousSteps.WAITLOOP,
            AutonomousSteps.SHOOTNOTE
          }
    };

    if (m_autoStep.length < m_cmdSteps.length ) {
      System.out.println("WARNING - Auto Commands LT Command Steps");
    }
    // more? like more commands than supported by the switch

  }

  private void fmtDisplay(int ix) {
  
    String labelName = "Step " + ix;
  
    m_tab
      .addString(labelName, () -> m_strStepList[ix])
      .withWidget(BuiltInWidgets.kTextView)
      .withSize(2,1)
      .withPosition(ix * 2, 3);

    labelName = "Switch(es) " + ix;
    m_tab
      .addString(labelName, () -> m_strStepSwitch[ix])
      .withPosition(ix *2, 4)
      .withSize(2, 1)
      .withWidget(BuiltInWidgets.kTextView);

    labelName = "SwState " + ix;
    m_tab
      .addBoolean(labelName, () -> m_bStepSWList[ix])
      .withPosition(ix *2, 5)
      .withSize(2, 1)
      .withWidget(BuiltInWidgets.kBooleanBox);

  }

  private void initStepList(int ix) {
      m_strStepList[ix] = "";
      m_strStepSwitch[ix] = "";
      m_bStepSWList[ix] = false;
  }

    @Override
    public void periodic() {
    // This method will be called once per scheduler run
    }
   

  public void selectAutoCommand() {

    int autoSelectIx = m_ConsoleAuto.getROT_SW_0();
    m_iPatternSelect = autoSelectIx;
    if (autoSelectIx >= m_cmdSteps.length) {
      autoSelectIx = 0;
      m_iPatternSelect = 0;
    }
    if (DriverStation.isDSAttached()) {
      //System.out.println(DriverStation.getAlliance().toString());
      boolean isAllianceRed = (DriverStation.getAlliance().get() == DriverStation.Alliance.Red);
      m_allianceColor.setBoolean(isAllianceRed);
    } else {
      m_allianceColor.setBoolean(true);
    }

    m_selectedCommand = m_autoSelectCommand[autoSelectIx];
    m_strCommand = m_selectedCommand.toString();
    m_autoCmd.setString(m_strCommand);

    m_iWaitCount = m_ConsoleAuto.getROT_SW_1();
    m_iWaitLoop.setValue(m_iWaitCount);

    m_iCmdCount = 0;
    for (int ix = 0; ix < m_cmdSteps[autoSelectIx].length; ix++) {
      m_autoStep[ix] = m_cmdSteps[autoSelectIx][ix];
      m_strStepList[ix] = m_autoStep[ix].name();
      m_strStepSwitch[ix] = getStepSwitch(m_autoStep[ix]);
      m_bStepSWList[ix] = getStepBoolean(m_autoStep[ix]);
      if (m_bStepSWList[ix]) {
        m_iCmdCount++;
      }
    }
    for (int ix = m_cmdSteps[autoSelectIx].length; ix < kSTEP_MAX; ix++) {
      initStepList(ix);
    }

  }

  private String getStepSwitch(AutonomousSteps stepName) {
    String stepSwName = "";
    int stepSwitch = stepName.getASwitch();
    if (stepSwitch > 0) {
      stepSwName = String.valueOf(stepSwitch);
    }
    stepSwitch = stepName.getBSwitch();
    if (stepSwitch > 0) {
      stepSwName = stepSwName + " & !" + String.valueOf(stepSwitch);
    }
    return stepSwName;
  }
    
  private boolean getStepBoolean(AutonomousSteps stepName) {
    boolean stepBool = true;
    int stepSwitch = stepName.getASwitch();
    if (stepSwitch > 0) {
      stepBool = m_ConsoleAuto.getButton(stepSwitch);
    }
    stepSwitch = stepName.getBSwitch();
    if (stepSwitch > 0) {
      stepBool = stepBool & !m_ConsoleAuto.getButton(stepSwitch);
    }
    return stepBool;
  }


  /*
   * Command to run the Auto selection process with Operator Console interaction
   * This should be handled by a trigger that is started on Disabled status
   */
  public Command cmdAutoSelect() {
    return Commands.run(this::selectAutoCommand, this)
          .ignoringDisable(true);
  }

  /*
   * Command to process the selected command list
  */
  public Command cmdAutoControl() {

    Command autoCmdList[] = new Command[m_iCmdCount];

    int cmdIx = 0;
    for (int ix = 0; ix < m_cmdSteps[m_iPatternSelect].length; ix++) {
      if (m_bStepSWList[ix]) {
        autoCmdList[cmdIx] = getAutoCmd(m_autoStep[ix]);
        cmdIx++;
      }
    }

    SequentialCommandGroup autoCmd = new SequentialCommandGroup(autoCmdList);
    return autoCmd;
   
  }
  private Command getAutoCmd(AutonomousSteps autoStep) {

    Command workCmd = Commands.print("command not found for " + autoStep.name());
    switch (autoStep.getStepStruc()) {
      case 'W':
        double waitTime = autoStep.getWaitTIme();
        workCmd = getWaitCommand(waitTime == 99.9 ? m_ConsoleAuto.getROT_SW_1() : waitTime);
        break;
      case 'D':
        workCmd =  m_robotContainer.getDrivePathCommand(autoStep.toString());
        break;
      case 'I':
        workCmd =  m_robotContainer.getIntakePathCommand(autoStep.toString(), autoStep.getWaitTIme());
        break;
      case 'S':
        //workCmd = getWaitCommand(2);
        switch (autoStep) {
          case SHOOTNOTE:
            workCmd = m_robotContainer.cmdShootNote().withTimeout(2);
            break;
        
          default:
            break;
        }
        break;
      default:
        break;
    }
    return workCmd;
  }

  private Command getWaitCommand(double seconds) {
    return Commands.waitSeconds(seconds);
  }
  
}
