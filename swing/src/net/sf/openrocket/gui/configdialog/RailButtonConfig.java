package net.sf.openrocket.gui.configdialog;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.barrowman.RailButtonCalc;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.CustomFocusTraversalPolicy;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.RailButton;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.OpenRocket;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Transformation;
import net.sf.openrocket.utils.educoder.Result;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.lang.reflect.Field;

import static net.sf.openrocket.gui.configdialog.NoseConeConfig.roundToFiveDecimals;

@SuppressWarnings("serial")
public class RailButtonConfig extends RocketComponentConfig {

    private static final Translator trans = Application.getTranslator();

    public RailButtonConfig(OpenRocketDocument document, RocketComponent component, JDialog parent) {
        super(document, component, parent);

        //// General and General properties
        tabbedPane.insertTab(trans.get("RailBtnCfg.tab.General"), null, buttonTab((RailButton) component), trans.get("RailBtnCfg.tab.GeneralProp"), 0);
        tabbedPane.setSelectedIndex(0);

        // Apply the custom focus travel policy to this panel
        //// Make sure the cancel & ok button is the last component
        order.add(cancelButton);
        order.add(okButton);
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
        parent.setFocusTraversalPolicy(policy);
    }

    private JPanel buttonTab(final RailButton rbc) {

        JPanel primary = new JPanel(new MigLayout());

        JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0"));


        { //// Outer Diameter
            panel.add(new JLabel(trans.get("RailBtnCfg.lbl.OuterDiam")));
            DoubleModel odModel = new DoubleModel(component, "OuterDiameter", UnitGroup.UNITS_LENGTH, 0);
            register(odModel);
            JSpinner odSpinner = new JSpinner(odModel.getSpinnerModel());
            odSpinner.setEditor(new SpinnerEditor(odSpinner));
            panel.add(odSpinner, "growx");
            order.add(((SpinnerEditor) odSpinner.getEditor()).getTextField());
            panel.add(new UnitSelector(odModel), "growx");
            panel.add(new BasicSlider(odModel.getSliderModel(0, 0.02)), "w 100lp, wrap");
        }
        { //// Inner Diameter
            panel.add(new JLabel(trans.get("RailBtnCfg.lbl.InnerDiam")));
            DoubleModel idModel = new DoubleModel(component, "InnerDiameter", UnitGroup.UNITS_LENGTH, 0);
            register(idModel);
            JSpinner idSpinner = new JSpinner(idModel.getSpinnerModel());
            idSpinner.setEditor(new SpinnerEditor(idSpinner));
            panel.add(idSpinner, "growx");
            order.add(((SpinnerEditor) idSpinner.getEditor()).getTextField());
            panel.add(new UnitSelector(idModel), "growx");
            panel.add(new BasicSlider(idModel.getSliderModel(0, 0.02)), "w 100lp, wrap 20lp");
        }
        { //// Base Height
            panel.add(new JLabel(trans.get("RailBtnCfg.lbl.BaseHeight")));
            DoubleModel heightModel = new DoubleModel(component, "BaseHeight", UnitGroup.UNITS_LENGTH, 0);
            register(heightModel);
            JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
            heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
            panel.add(heightSpinner, "growx");
            order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
            panel.add(new UnitSelector(heightModel), "growx");
            panel.add(new BasicSlider(heightModel.getSliderModel(0, new DoubleModel(component, "MaxBaseHeight", UnitGroup.UNITS_LENGTH))),
                    "w 100lp, wrap");
        }
        { //// Flange Height
            panel.add(new JLabel(trans.get("RailBtnCfg.lbl.FlangeHeight")));
            DoubleModel heightModel = new DoubleModel(component, "FlangeHeight", UnitGroup.UNITS_LENGTH, 0);
            register(heightModel);
            JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
            heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
            panel.add(heightSpinner, "growx");
            order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
            panel.add(new UnitSelector(heightModel), "growx");
            panel.add(new BasicSlider(heightModel.getSliderModel(0, new DoubleModel(component, "MaxFlangeHeight", UnitGroup.UNITS_LENGTH))),
                    "w 100lp, wrap");
        }
        { //// Total Height
            panel.add(new JLabel(trans.get("RailBtnCfg.lbl.TotalHeight")));
            DoubleModel heightModel = new DoubleModel(component, "TotalHeight", UnitGroup.UNITS_LENGTH, 0);
            register(heightModel);
            JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
            heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
            panel.add(heightSpinner, "growx");
            order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
            panel.add(new UnitSelector(heightModel), "growx");
            panel.add(new BasicSlider(heightModel.getSliderModel(new DoubleModel(component, "MinTotalHeight", UnitGroup.UNITS_LENGTH), 0.02)),
                    "w 100lp, wrap 20lp");
        }
        { //// Screw height
            panel.add(new JLabel(trans.get("RailBtnCfg.lbl.ScrewHeight")));
            DoubleModel heightModel = new DoubleModel(component, "ScrewHeight", UnitGroup.UNITS_LENGTH, 0);
            register(heightModel);
            JSpinner heightSpinner = new JSpinner(heightModel.getSpinnerModel());
            heightSpinner.setEditor(new SpinnerEditor(heightSpinner));
            panel.add(heightSpinner, "growx");
            order.add(((SpinnerEditor) heightSpinner.getEditor()).getTextField());
            panel.add(new UnitSelector(heightModel), "growx");
            panel.add(new BasicSlider(heightModel.getSliderModel(0, 0.02)), "w 100lp, wrap 30lp");
        }

        // -------- Instances ------
        InstancesPanel ip = new InstancesPanel(component, order);
        register(ip);
        panel.add(ip, "span, grow, wrap para");


        primary.add(panel, "grow, gapright 40lp");

        // Right side panel
        panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

        {// -------- Placement ------
            //// Position relative to:
            PlacementPanel placementPanel = new PlacementPanel(component, order);
            register(placementPanel);
            panel.add(placementPanel, "span, grow, wrap");

            { //// Rotation:
                placementPanel.add(new JLabel(trans.get("RailBtnCfg.lbl.Angle")), "newline");
                DoubleModel angleModel = new DoubleModel(component, "AngleOffset", UnitGroup.UNITS_ANGLE, -180, +180);
                register(angleModel);
                JSpinner angleSpinner = new JSpinner(angleModel.getSpinnerModel());
                angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
                placementPanel.add(angleSpinner, "growx");
                order.add(((SpinnerEditor) angleSpinner.getEditor()).getTextField());
                placementPanel.add(new UnitSelector(angleModel), "growx");
                placementPanel.add(new BasicSlider(angleModel.getSliderModel(-Math.PI, Math.PI)), "w 100lp");
            }
        }

        //// Material
        MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
        register(materialPanel);
        panel.add(materialPanel, "span, grow, wrap");


        primary.add(panel, "grow");

        {//// CG calculation demonstration
            panel.add(new JLabel(trans.get("RailButton.lbl.CgCalc") + ":"));
            JButton button = new JButton(trans.get("RailButton.lbl.CgEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("RailButton.lbl.CgCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));


                net.sf.openrocket.utils.educoder.RailButtonCgRequest request = new net.sf.openrocket.utils.educoder.RailButtonCgRequest();
                request.setAnswer(component.getComponentCG().x);

                try {
                    Field field = RailButton.class.getDeclaredField("instanceSeparation");
                    field.setAccessible(true);
                    Double o = (Double) field.get(component);
                    request.setInstanceSeparation(o);
                    Field field2 = RailButton.class.getDeclaredField("instanceCount");
                    field2.setAccessible(true);

                    Integer o2 = (Integer) field2.get(component);
                    request.setInstanceCount(o2);

                    String constraints = "newline, height 30!";
                    String labelText2 = trans.get("InstancesPanel.lbl.InstanceCount") + request.getInstanceCount();
                    dialog.add(new JLabel(labelText2), constraints);
                    String labelText3 = trans.get("InstancesPanel.lbl.InstanceSeparation") + request.getInstanceSeparation();
                    dialog.add(new JLabel(labelText3), constraints);

                } catch (Exception ex) {
                    //ignore
                }
                JButton checkButton = new JButton(trans.get("RailButton.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("RailButton.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("RailButton.lbl.answer") + ": ");
                dialog.add(checkButton, "newline, height 30!");
                dialog.add(checkResult, "height 30!");
                dialog.add(answerLabel, "height 30!");
                // Do not use UI thread to get the answer
                checkButton.addActionListener(e1 -> OpenRocket.eduCoderService.calculateCG(request).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NotNull Call<net.sf.openrocket.utils.educoder.Result> call, @NotNull Response<net.sf.openrocket.utils.educoder.Result> response) {
                        net.sf.openrocket.utils.educoder.Result result = response.body();
                        if (result == null) return;
                        Integer code = response.body().getCode();
                        if (code == 200) {
                            SwingUtilities.invokeLater(() -> {
                                checkResult.setText(trans.get("RailButton.lbl.checkResult") + ": " + result.getResult());
                                String msg = "";
                                double answer = component.getComponentCG().x;
                                double resultResult = (double) result.getResult();
                                if (roundToFiveDecimals(answer)==roundToFiveDecimals(resultResult)){
                                    msg = "答案:"+String.valueOf(answer);
                                }else {
                                    double absError = Math.abs(resultResult - answer) * 0.1;
                                    double relativeError = (answer != 0) ? (absError / Math.abs(answer)) * 100 : 0;
                                    msg = "误差："+String.valueOf(relativeError)+"%";
                                }
                                answerLabel.setText(msg);
                            });
                        } else {
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(parent, response.body().getResult(), "Error", JOptionPane.ERROR_MESSAGE));
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<net.sf.openrocket.utils.educoder.Result> call, @NotNull Throwable throwable) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(parent, throwable.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                    }
                }));
                dialog.setVisible(true);
            });
        }

        {//// CP calculation demonstration
            panel.add(new JLabel(trans.get("RailButton.lbl.CpCalc") + ":"));
            JButton button = new JButton(trans.get("RailButton.lbl.CpEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("RailButton.lbl.CpCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.RailButtonCpRequest request = new net.sf.openrocket.utils.educoder.RailButtonCpRequest();


                FlightConfiguration curConfig = document.getSelectedConfiguration();
                FlightConditions conditions = new FlightConditions(curConfig);
                RailButtonCalc componentCalc = new RailButtonCalc(component);


                AerodynamicForces forces = new AerodynamicForces().zero();
                componentCalc.calculateNonaxialForces(conditions, new Transformation(0, 0, 0), forces, new WarningSet());
                request.setAnswer(forces.getCP().x);


                JButton checkButton = new JButton(trans.get("RailButton.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("RailButton.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("RailButton.lbl.answer") + ": ");
                dialog.add(checkButton, "newline, height 30!");
                dialog.add(checkResult, "height 30!");
                dialog.add(answerLabel, "height 30!");
                // Do not use UI thread to get the answer
                checkButton.addActionListener(e1 -> OpenRocket.eduCoderService.calculateCP(request).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NotNull Call<net.sf.openrocket.utils.educoder.Result> call, @NotNull Response<net.sf.openrocket.utils.educoder.Result> response) {
                        net.sf.openrocket.utils.educoder.Result result = response.body();
                        if (result == null) return;
                        Integer code = response.body().getCode();
                        if (code == 200) {
                            SwingUtilities.invokeLater(() -> {
                                checkResult.setText(trans.get("RailButton.lbl.checkResult") + ": " + result.getResult());
                                String msg = "";
                                double answer =forces.getCP().x;
                                double resultResult = (double) result.getResult();
                                if (roundToFiveDecimals(answer)==roundToFiveDecimals(resultResult)){
                                    msg = "答案:"+String.valueOf(answer);
                                }else {
                                    double absError = Math.abs(resultResult - answer) * 0.1;
                                    double relativeError = (answer != 0) ? (absError / Math.abs(answer)) * 100 : 0;
                                    msg = "误差："+String.valueOf(relativeError)+"%";
                                }
                                answerLabel.setText(msg);
                            });
                        } else {
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(parent, response.body().getResult(), "Error", JOptionPane.ERROR_MESSAGE));
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<Result> call, @NotNull Throwable throwable) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(parent, throwable.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                    }
                }));
                dialog.setVisible(true);
            });
        }

        {//// MOI calculation demonstration
            panel.add(new JLabel(trans.get("RailButton.lbl.MOICal") + ":"));
            JButton button = new JButton(trans.get("RailButton.lbl.MOIEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("RailButton.lbl.MOICal"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.RailButtonMOIRequest request = new net.sf.openrocket.utils.educoder.RailButtonMOIRequest();
                request.setAnswer(new Double[]{component.getRotationalUnitInertia(), component.getLongitudinalUnitInertia()});

                JButton checkButton = new JButton(trans.get("RailButton.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("RailButton.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("RailButton.lbl.answer") + ": ");
                dialog.add(checkButton, "newline, height 30!");
                dialog.add(checkResult, "height 30!");
                dialog.add(answerLabel, "height 30!");
                // Do not use UI thread to get the answer
                checkButton.addActionListener(e1 -> OpenRocket.eduCoderService.calculateMOI(request).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NotNull Call<net.sf.openrocket.utils.educoder.Result2> call, @NotNull Response<net.sf.openrocket.utils.educoder.Result2> response) {
                        net.sf.openrocket.utils.educoder.Result2 result = response.body();
                        if (result == null) return;
                        Integer code = response.body().getCode();
                        if (code == 200) {
                            SwingUtilities.invokeLater(() -> {
                                checkResult.setText(trans.get("TubeFinSet.lbl.checkResult") + ": " + result.getResult()[0] + "," + result.getResult()[1]);
                                String msg = "";
                                String msg2 = "";
                                double answer =  component.getRotationalUnitInertia();
                                double answer2 = component.getLongitudinalUnitInertia();

                                double resultResult = (double) result.getResult()[0];
                                double resultResult2 = (double) result.getResult()[1];
                                if (roundToFiveDecimals(answer)==roundToFiveDecimals(resultResult)){
                                    msg = "答案:"+String.valueOf(answer+","+answer2);
                                }else {
                                    double absError = Math.abs(resultResult - answer) * 0.1;
                                    double relativeError = (answer != 0) ? (absError / Math.abs(answer)) * 100 : 0;
                                    msg = "误差："+String.valueOf(relativeError)+"%";

                                    double absError2 = Math.abs(resultResult2 - answer2) * 0.1;
                                    double relativeError2 = (answer2 != 0) ? (absError2 / Math.abs(answer2)) * 100 : 0;
                                    msg2 = String.valueOf(relativeError2)+"%";
                                }
                                answerLabel.setText(msg+","+msg2);
                            });
                        } else {
                            SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(parent, response.body().getResult(), "Error", JOptionPane.ERROR_MESSAGE));
                        }
                    }

                    @Override
                    public void onFailure(@NotNull Call<net.sf.openrocket.utils.educoder.Result2> call, @NotNull Throwable throwable) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(parent, throwable.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                    }
                }));
                dialog.setVisible(true);
            });
        }

        return primary;
    }

    @Override
    public void updateFields() {
        super.updateFields();
    }

}
