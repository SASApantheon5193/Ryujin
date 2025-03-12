// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import frc.robot.Constants.AutoConstants;
import frc.robot.Constants.DriveConstants;
import frc.robot.Constants.OIConstants;
import frc.robot.subsystems.AlgaeSubsystem;
import frc.robot.subsystems.CoralSubsystem;
import frc.robot.subsystems.CoralSubsystem.Setpoint;
import frc.robot.subsystems.DriveSubsystem;
import java.util.List;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser;



/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems
  private final DriveSubsystem m_robotDrive = new DriveSubsystem();
  private final CoralSubsystem m_coralSubSystem = new CoralSubsystem();
  private final AlgaeSubsystem m_algaeSubsystem = new AlgaeSubsystem();



  // The driver's controller
  CommandXboxController m_driverController =
      new CommandXboxController(OIConstants.kDriverControllerPort);

           /* Path follower */
     private final SendableChooser<Command> autoChooser;

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {

    NamedCommands.registerCommand("score", m_coralSubSystem.reverseIntakeCommand());
    
    // Configure the button bindings
    configureButtonBindings();
    
    autoChooser = AutoBuilder.buildAutoChooser("Tests");
        SmartDashboard.putData("Auto Mode", autoChooser);
        autoChooser.addOption("Blue Middle", new PathPlannerAuto("Blue Middle"));

    // Configure default commands
    m_robotDrive.setDefaultCommand(
        // The left stick controls translation of the robot.
        // Turning is controlled by the X axis of the right stick.
        new RunCommand(
            () ->
                m_robotDrive.drive(
                    -MathUtil.applyDeadband(
                        m_driverController.getLeftY(), OIConstants.kDriveDeadband),
                    -MathUtil.applyDeadband(
                        m_driverController.getLeftX(), OIConstants.kDriveDeadband),
                    -MathUtil.applyDeadband(
                        m_driverController.getRightX(), OIConstants.kDriveDeadband),
                    true),
            m_robotDrive));

    // Set the ball intake to in/out when not running based on internal state
    m_algaeSubsystem.setDefaultCommand(m_algaeSubsystem.idleCommand());
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling passing it to a
   * {@link JoystickButton}.
   */
  private void configureButtonBindings() {
    // Left Stick Button -> Set swerve to X
    m_driverController.leftStick().whileTrue(m_robotDrive.setXCommand());

    // Left Bumper -> Run tube intake
    m_driverController.rightBumper().whileTrue(m_coralSubSystem.runIntakeCommand());

    // Right Bumper -> Run tube intake in reverse
    m_driverController.leftBumper().whileTrue(m_coralSubSystem.reverseIntakeCommand());

    //m_driverController.rightStick().whileTrue(m_robotDrive.slowDriveCommand());

    // B Button -> Elevator/Arm to human player position, set ball intake to stow
    // when idle
    m_driverController
        .b()
        .onTrue(
            m_coralSubSystem
                .setSetpointCommand(Setpoint.kFeederStation));

    // A Button -> Elevator/Arm to level 2 position
    m_driverController.a().onTrue(m_coralSubSystem.setSetpointCommand(Setpoint.kLevel2));

    // X Button -> Elevator/Arm to level 3 position
    m_driverController.x().onTrue(m_coralSubSystem.setSetpointCommand(Setpoint.kLevel3));

    // Y Button -> Elevator/Arm to level 4 position
    m_driverController.y().onTrue(m_coralSubSystem.setSetpointCommand(Setpoint.kLevel4));

    // Right Trigger -> Run ball intake, set to leave out when idle
    m_driverController
        .leftTrigger(OIConstants.kTriggerButtonThreshold)
        .whileTrue(m_algaeSubsystem.runIntakeCommand());

    // Left Trigger -> Run ball intake in reverse, set to stow when idle
    m_driverController
        .rightTrigger(OIConstants.kTriggerButtonThreshold)
        .whileTrue(m_algaeSubsystem.reverseIntakeCommand());
    
    // Start Button -> Zero swerve heading
    m_driverController.start().onTrue(m_robotDrive.zeroHeadingCommand());
  }

  public double getSimulationTotalCurrentDraw() {
    // for each subsystem with simulation
    return m_coralSubSystem.getSimulationCurrentDraw();
  }

  public Command getAutonomousCommand() {
    /* Run the path selected from the auto chooser */
    return autoChooser.getSelected();

}
}