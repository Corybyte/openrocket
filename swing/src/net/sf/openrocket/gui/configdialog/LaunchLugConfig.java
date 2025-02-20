package net.sf.openrocket.gui.configdialog;


import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.barrowman.LaunchLugCalc;
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
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.OpenRocket;
import net.sf.openrocket.unit.UnitGroup;

import net.sf.openrocket.util.Transformation;
import net.sf.openrocket.utils.educoder.LaunchLugCgRequest;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static net.sf.openrocket.gui.configdialog.NoseConeConfig.roundToFiveDecimals;

@SuppressWarnings("serial")
public class LaunchLugConfig extends RocketComponentConfig {

    private static final Translator trans = Application.getTranslator();

    public LaunchLugConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
        super(d, c, parent);

        JPanel primary = new JPanel(new MigLayout());


        JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

        ////  Body tube length
        //// Length:
        panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Length")));

        DoubleModel m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
        register(m);

        JSpinner spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        focusElement = spin;
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.1)), "w 100lp, wrap 20lp");


        //// Body tube diameter
        //// Outer diameter:
        panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Outerdiam")));

        DoubleModel od = new DoubleModel(component, "OuterRadius", 2, UnitGroup.UNITS_LENGTH, 0);
        register(od);
        // Diameter = 2*Radius

        spin = new JSpinner(od.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(od), "growx");
        panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap rel");


        ////  Inner diameter:
        panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Innerdiam")));

        // Diameter = 2*Radius
        m = new DoubleModel(component, "InnerRadius", 2, UnitGroup.UNITS_LENGTH, 0);
        register(m);


        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(new DoubleModel(0), od)), "w 100lp, wrap rel");


        ////  Wall thickness
        //// Thickness:
        panel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Thickness")));

        m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 30lp");

        // -------- Instances ------
        InstancesPanel ip = new InstancesPanel(component, order);
        register(ip);
        panel.add(ip, "span, grow, wrap para");

        primary.add(panel, "grow, gapright 40lp");


        {//// CG calculation demonstration
            panel.add(new JLabel(trans.get("LaunchLug.lbl.CgCalc") + ":"));
            JButton button = new JButton(trans.get("LaunchLug.lbl.CgEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("LaunchLug.lbl.CgCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));


                net.sf.openrocket.utils.educoder.LaunchLugCgRequest request = new LaunchLugCgRequest();
                request.setLength(component.getLength());
                request.setAnswer(component.getComponentCG().x);

                try {
                    Field field = LaunchLug.class.getDeclaredField("instanceSeparation");
                    field.setAccessible(true);
                    Double o = (Double) field.get(component);
                    request.setInstanceSeparation(o);
                    Field field2 = LaunchLug.class.getDeclaredField("instanceCount");
                    field2.setAccessible(true);

                    Integer o2 = (Integer) field2.get(component);
                    request.setInstanceCount(o2);

                    String labelText = "长度: " + request.getLength();
                    String constraints = "newline, height 30!";
                    dialog.add(new JLabel(labelText), constraints);
                    String labelText2 = trans.get("InstancesPanel.lbl.InstanceCount") + request.getInstanceCount();
                    dialog.add(new JLabel(labelText2), constraints);
                    String labelText3 = trans.get("InstancesPanel.lbl.InstanceSeparation") + request.getInstanceSeparation();
                    dialog.add(new JLabel(labelText3), constraints);
                } catch (Exception ex) {
                    //ignore
                }
                JButton checkButton = new JButton(trans.get("LaunchLug.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("LaunchLug.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("LaunchLug.lbl.answer") + ": ");
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
                                checkResult.setText(trans.get("LaunchLug.lbl.checkResult") + ": " + result.getResult());
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
            panel.add(new JLabel(trans.get("LaunchLug.lbl.CpCalc") + ":"));
            JButton button = new JButton(trans.get("LaunchLug.lbl.CpEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("LaunchLug.lbl.CpCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                net.sf.openrocket.utils.educoder.LaunchLugCpRequest request = new net.sf.openrocket.utils.educoder.LaunchLugCpRequest();
                FlightConfiguration curConfig = document.getSelectedConfiguration();
                FlightConditions conditions = new FlightConditions(curConfig);
                LaunchLugCalc componentCalc = new LaunchLugCalc(component);

                AerodynamicForces forces = new AerodynamicForces().zero();
                componentCalc.calculateNonaxialForces(conditions, new Transformation(0, 0, 0), forces, new WarningSet());
                request.setAnswer(forces.getCP().x);

                JButton checkButton = new JButton(trans.get("LaunchLug.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("LaunchLug.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("LaunchLug.lbl.answer") + ": ");
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
                                checkResult.setText(trans.get("LaunchLug.lbl.checkResult") + ": " + result.getResult());
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
                    public void onFailure(@NotNull Call<net.sf.openrocket.utils.educoder.Result> call, @NotNull Throwable throwable) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(parent, throwable.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                    }
                }));
                dialog.setVisible(true);
            });
        }
        {//// MOI calculation demonstration
            panel.add(new JLabel(trans.get("LaunchLug.lbl.MOICal") + ":"));
            JButton button = new JButton(trans.get("LaunchLug.lbl.MOIEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("LaunchLug.lbl.MOICal"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));


                net.sf.openrocket.utils.educoder.LaunchLugMOIRequest request = new net.sf.openrocket.utils.educoder.LaunchLugMOIRequest();
                request.setAnswer(new Double[]{component.getRotationalUnitInertia(), component.getLongitudinalUnitInertia()});
                request.setLength(c.getLength());
                String[] methodNames = {"getOuterRadius", "getInnerRadius"};
                try {
                    for (String methodName : methodNames) {
                        Method declaredMethod = LaunchLug.class.getDeclaredMethod(methodName);
                        Method reqMethod = net.sf.openrocket.utils.educoder.LaunchLugMOIRequest.class.getDeclaredMethod(
                                methodName.replaceFirst("get", "set"), Double.class);
                        declaredMethod.setAccessible(true);
                        reqMethod.setAccessible(true);
                        Double value = (Double) declaredMethod.invoke(component);
                        reqMethod.invoke(request, value);
                        String labelText = trans.get("LaunchLug.lbl." + methodName.replaceFirst("get", "")) + ": " + value;
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                JButton checkButton = new JButton(trans.get("LaunchLug.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("LaunchLug.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("LaunchLug.lbl.answer") + ": ");
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
        // Right panel
        panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

        {//// Placement
            //// Position relative to:
            PlacementPanel placementPanel = new PlacementPanel(component, order);
            register(placementPanel);
            panel.add(placementPanel, "span, grow, wrap");

            ////  Rotation:
            placementPanel.add(new JLabel(trans.get("LaunchLugCfg.lbl.Angle")), "newline");

            m = new DoubleModel(component, "AngleOffset", UnitGroup.UNITS_ANGLE, -Math.PI, Math.PI);
            register(m);

            spin = new JSpinner(m.getSpinnerModel());
            spin.setEditor(new SpinnerEditor(spin));
            placementPanel.add(spin, "growx");
            order.add(((SpinnerEditor) spin.getEditor()).getTextField());

            placementPanel.add(new UnitSelector(m), "growx");
            placementPanel.add(new BasicSlider(m.getSliderModel()), "w 100lp, wrap");
        }


        //// Material
        MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
        register(materialPanel);
        panel.add(materialPanel, "span, grow, wrap");

        primary.add(panel, "grow");

        //// General and General properties
        tabbedPane.insertTab(trans.get("LaunchLugCfg.tab.General"), null, primary,
                trans.get("LaunchLugCfg.tab.Generalprop"), 0);
        tabbedPane.setSelectedIndex(0);

        // Apply the custom focus travel policy to this config dialog
        //// Make sure the cancel & ok button is the last component
        order.add(cancelButton);
        order.add(okButton);
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
        parent.setFocusTraversalPolicy(policy);
    }

    @Override
    public void updateFields() {
        super.updateFields();
    }

}
