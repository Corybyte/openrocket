package net.sf.openrocket.gui.configdialog;


import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.barrowman.TubeFinSetCalc;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.CustomFocusTraversalPolicy;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.*;
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
import java.lang.reflect.Method;

import static net.sf.openrocket.gui.configdialog.NoseConeConfig.roundToFiveDecimals;

public class TubeFinSetConfig extends RocketComponentConfig {
    private static final long serialVersionUID = 508482875624928676L;
    private static final Translator trans = Application.getTranslator();

    public TubeFinSetConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
        super(d, c, parent);

        JPanel primary = new JPanel(new MigLayout());


        JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

        ////  Number of fins
        panel.add(new JLabel(trans.get("TubeFinSetCfg.lbl.Nbroffins")));

        IntegerModel im = new IntegerModel(component, "FinCount", 1, 8);
        register(im);

        JSpinner spin = new JSpinner(im.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx, wrap");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        //// Length:
        panel.add(new JLabel(trans.get("TubeFinSetCfg.lbl.Length")));

        DoubleModel m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        focusElement = spin;
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(0, 0.02, 0.1)), "w 100lp, wrap para");


        //// Outer diameter:
        panel.add(new JLabel(trans.get("TubeFinSetCfg.lbl.Outerdiam")));

        DoubleModel od = new DoubleModel(component, "OuterRadius", 2, UnitGroup.UNITS_LENGTH, 0);
        register(od);

        spin = new JSpinner(od.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(od), "growx");
        panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap rel");

        JCheckBox check = new JCheckBox(od.getAutomaticAction());
        //// Automatic
        check.setText(trans.get("TubeFinSetCfg.checkbox.Automatic"));
        panel.add(check, "skip, span 2, wrap");
        order.add(check);

        ////  Inner diameter:
        panel.add(new JLabel(trans.get("TubeFinSetCfg.lbl.Innerdiam")));

        m = new DoubleModel(component, "InnerRadius", 2, UnitGroup.UNITS_LENGTH, 0);
        register(m);
        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(new DoubleModel(0), od)), "w 100lp, wrap rel");


        //// Thickness:
        panel.add(new JLabel(trans.get("TubeFinSetCfg.lbl.Thickness")));

        m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 20lp");

        primary.add(panel, "grow, gapright 40lp");


        {//// CG calculation demonstration
            panel.add(new JLabel(trans.get("TubeFinSet.lbl.CgCalc") + ":"));
            JButton button = new JButton(trans.get("TubeFinSet.lbl.CgEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("TubeFinSet.lbl.CgCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.TubeFinsetCGRequest request = new net.sf.openrocket.utils.educoder.TubeFinsetCGRequest();
                request.setAnswer(component.getComponentCG().x);

                String[] MethodNames = {"getOuterRadius", "getBodyRadius", "getInnerRadius"};
                String[] fieldNames = {"fins", "thickness"};
                //volume  length

                try {
                    for (String fieldName : fieldNames) {
                        Field field = TubeFinSet.class.getDeclaredField(fieldName);
                        Field reqField = net.sf.openrocket.utils.educoder.TubeFinsetCGRequest.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        reqField.setAccessible(true);
                        Object value = field.get(component);
                        reqField.set(request, value);
                        String labelText = trans.get("TubeFinSet.lbl." + fieldName) + ": " + value;
                        String constraints = (fieldName.equals(fieldNames[0])) ? "spanx, height 30!" : "newline, height 30!";
                        dialog.add(new JLabel(labelText), constraints);
                    }
                    // Component Length
                    double length = component.getLength();
                    request.setLength(length);
                    String lengthLabelText = trans.get("TubeFinSet.lbl.length") + ": " + length;
                    dialog.add(new JLabel(lengthLabelText), "newline, height 30!");
                    // Material Density
                    double density = ((TubeFinSet) component).getMaterial().getDensity();
                    request.setDensity(density);
                    lengthLabelText = trans.get("TubeFinSet.lbl.density") + ": " + density;
                    dialog.add(new JLabel(lengthLabelText), "newline, height 30!");

                    for (String methodName : MethodNames) {
                        Method method = TubeFinSet.class.getDeclaredMethod(methodName);
                        Method reqMethod = net.sf.openrocket.utils.educoder.TubeFinsetCGRequest.class
                                .getDeclaredMethod(methodName.replaceFirst("get", "set"), Double.class);
                        Double value = (Double) method.invoke(component); // All values are double type
                        reqMethod.invoke(request, value);
                        String labelText = trans.get("TubeFinSet.lbl." + methodName.replaceFirst("get", "")) + ": " + value;
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }
                    JButton checkButton = new JButton(trans.get("TubeFinSet.lbl.check"));
                    JLabel checkResult = new JLabel(trans.get("TubeFinSet.lbl.checkResult") + ": ");
                    JLabel answerLabel = new JLabel(trans.get("TubeFinSet.lbl.answer") + ": ");
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
                                    checkResult.setText(trans.get("NoseConeCfg.lbl.checkResult") + ": " + result.getResult());
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
                } catch (Exception ex) {
                    // ignored
                }
                dialog.setVisible(true);
            });
        }

        {//// CP calculation demonstration
            panel.add(new JLabel(trans.get("TubeFinSet.lbl.CpCalc") + ":"));
            JButton button = new JButton(trans.get("TubeFinSet.lbl.CpEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("TubeFinSet.lbl.CpCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.TubeFinSetCpRequest request = new net.sf.openrocket.utils.educoder.TubeFinSetCpRequest();

                FlightConfiguration curConfig = document.getSelectedConfiguration();
                FlightConditions conditions = new FlightConditions(curConfig);

                String outerRadius = "getOuterRadius";
                TubeFinSetCalc componentCalc = new TubeFinSetCalc(component);
                String[] fieldNames = {"chord", "ar"};
                String[] methodNames = {"getMach", "getBeta"}; //cond

                AerodynamicForces forces = new AerodynamicForces().zero();
                componentCalc.calculateNonaxialForces(conditions, new Transformation(0, 0, 0), forces, new WarningSet());
                request.setAnswer(forces.getCP().x);


                try {
                    //outerRadius
                    Method outerRadiusMethod = TubeFinSet.class.getDeclaredMethod(outerRadius);
                    Object o = outerRadiusMethod.invoke(component);
                    request.setOuterRadius((Double) o);
                    Field field1 = TubeFinSetCalc.class.getDeclaredField("poly");
                    field1.setAccessible(true);
                    double[] doubles = (double[]) field1.get(componentCalc);
                    request.setPloy(doubles);
                    String labelText2 = trans.get("TubeFinSet.lbl.OuterRadius") + "：" + o;
                    dialog.add(new JLabel(labelText2), "spanx, height 30!");
//                    String labelText3 = trans.get("TubeFinSet.lbl.ar") + Arrays.toString(doubles);
//                    dialog.add(new JLabel(labelText3),"spanx, height 30!");


                    for (String fieldName : fieldNames) {
                        Field field = TubeFinSetCalc.class.getDeclaredField(fieldName);
                        Field reqField = net.sf.openrocket.utils.educoder.TubeFinSetCpRequest.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        reqField.setAccessible(true);
                        Double value = (Double) field.get(componentCalc); // All values are double type
                        reqField.set(request, value);
                        String labelText = trans.get("TubeFinSet.lbl." + fieldName) + ": " + value;
                        String constraints = (fieldName.equals(fieldNames[0])) ? "spanx, height 30!" : "newline, height 30!";
                        dialog.add(new JLabel(labelText), constraints);
                    }
                    for (String methodName : methodNames) {
                        Method method = FlightConditions.class.getDeclaredMethod(methodName);
                        Method reqMethod = net.sf.openrocket.utils.educoder.TubeFinSetCpRequest.class.getDeclaredMethod(methodName.replaceFirst("get", "set"), Double.class);
                        Double value = (Double) method.invoke(conditions); // All values are double type
                        reqMethod.invoke(request, value);
                        String labelText = trans.get("TubeFinSet.lbl." + methodName.replaceFirst("get", "")) + ": " + value;
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }

                    JButton checkButton = new JButton(trans.get("TubeFinSet.lbl.check"));
                    JLabel checkResult = new JLabel(trans.get("TubeFinSet.lbl.checkResult") + ": ");
                    JLabel answerLabel = new JLabel(trans.get("TubeFinSet.lbl.answer") + ": ");
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
                                    checkResult.setText(trans.get("TubeFinSet.lbl.checkResult") + ": " + result.getResult());
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
                } catch (Exception ex) {
                    // ignored
                    ex.printStackTrace();
                }
                dialog.setVisible(true);
            });
        }

        { // MOI calculate:
            panel.add(new JLabel(trans.get("TubeFinSet.lbl.MOICal") + ":"));
            JButton button = new JButton(trans.get("TubeFinSet.lbl.MOIEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("TubeFinSet.lbl.MOICal"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.TubeFinSetMOIRequest request = new net.sf.openrocket.utils.educoder.TubeFinSetMOIRequest();
                // answer = rotationalUnitInertia
                component.getRotationalUnitInertia();
                request.setAnswer(new Double[]{component.getRotationalUnitInertia(), component.getLongitudinalUnitInertia()});
                String[] MethodNames = {"getOuterRadius", "getBodyRadius", "getInnerRadius"};
                String[] fieldNames = {"thickness", "fins"};

                try {
                    //get and set  properties
                    for (String fieldName : fieldNames) {
                        Field field = TubeFinSet.class.getDeclaredField(fieldName);
                        Field reqField = net.sf.openrocket.utils.educoder.TubeFinSetMOIRequest.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        reqField.setAccessible(true);
                        Object value = field.get(component);
                        reqField.set(request, value);
                        String labelText = trans.get("TubeFinSet.lbl." + fieldName) + ": " + value;
                        String constraints = (fieldName.equals(fieldNames[0])) ? "spanx, height 30!" : "newline, height 30!";
                        dialog.add(new JLabel(labelText), constraints);
                    }
                    // AftRadius
                    for (String methodName : MethodNames) {
                        Method method = TubeFinSet.class.getDeclaredMethod(methodName);
                        Method reqMethod = net.sf.openrocket.utils.educoder.TubeFinSetMOIRequest.class
                                .getDeclaredMethod(methodName.replaceFirst("get", "set"), Double.class);
                        Double value = (Double) method.invoke(component); // All values are double type
                        reqMethod.invoke(request, value);
                        String labelText = trans.get("TubeFinSet.lbl." + methodName.replaceFirst("get", "")) + ": " + value;
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }
                    request.setLength(c.getLength());
                    request.setAxialOffset(c.getAxialOffset());
                    JButton checkButton = new JButton(trans.get("TubeFinSet.lbl.check"));
                    JLabel checkResult = new JLabel(trans.get("TubeFinSet.lbl.checkResult") + ": ");
                    JLabel answerLabel = new JLabel(trans.get("TubeFinSet.lbl.answer") + ": ");
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

                } catch (Exception ex) {
                    //ignored
                }
                dialog.setVisible(true);
            });
        }

        // Right side panel
        panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]", ""));

        { //// Placement
            //// Position relative to:
            PlacementPanel placementPanel = new PlacementPanel(component, order);
            register(placementPanel);
            panel.add(placementPanel, "span, grow, wrap");

            //// Fin rotation:
            JLabel label = new JLabel(trans.get("TubeFinSetCfg.lbl.Finrotation"));
            //// The angle of the first fin in the fin set.
            label.setToolTipText(trans.get("TubeFinSetCfg.lbl.ttip.Finrotation"));
            placementPanel.add(label, "newline");

            m = new DoubleModel(component, "BaseRotation", UnitGroup.UNITS_ANGLE);
            register(m);

            spin = new JSpinner(m.getSpinnerModel());
            spin.setEditor(new SpinnerEditor(spin));
            placementPanel.add(spin, "growx");
            order.add(((SpinnerEditor) spin.getEditor()).getTextField());

            placementPanel.add(new UnitSelector(m), "growx");
            placementPanel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
        }

        {//// Material
            MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
            register(materialPanel);
            panel.add(materialPanel, "span, grow, wrap");
        }

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
