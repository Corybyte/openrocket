package net.sf.openrocket.gui.configdialog;


import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.barrowman.SymmetricComponentCalc;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.CustomFocusTraversalPolicy;
import net.sf.openrocket.gui.adaptors.DoubleModel;
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
import net.sf.openrocket.utils.educoder.BodyTubeCgRequest;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static net.sf.openrocket.gui.configdialog.NoseConeConfig.roundToFiveDecimals;

@SuppressWarnings("serial")
public class BodyTubeConfig extends RocketComponentConfig {

    private DoubleModel maxLength;
    private final JCheckBox checkAutoOuterRadius;
    private static final Translator trans = Application.getTranslator();

    public BodyTubeConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
        super(d, c, parent);

        JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]", ""));

        ////  Body tube length
        panel.add(new JLabel(trans.get("BodyTubecfg.lbl.Bodytubelength")));

        maxLength = new DoubleModel(2.0);
        DoubleModel length = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
        register(length);

        JSpinner spin = new JSpinner(length.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        focusElement = spin;
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(length), "growx");
        panel.add(new BasicSlider(length.getSliderModel(0, 0.5, maxLength)), "w 100lp, wrap");

        //// Body tube diameter
        panel.add(new JLabel(trans.get("BodyTubecfg.lbl.Outerdiameter")));

        // Diameter = 2*Radius
        final DoubleModel od = new DoubleModel(component, "OuterRadius", 2, UnitGroup.UNITS_LENGTH, 0);
        register(od);
        spin = new JSpinner(od.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(od), "growx");
        panel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap 0px");

        //// Automatic
        javax.swing.Action outerAutoAction = od.getAutomaticAction();
        checkAutoOuterRadius = new JCheckBox(outerAutoAction);
        checkAutoOuterRadius.setText(trans.get("BodyTubecfg.checkbox.Automatic"));
        panel.add(checkAutoOuterRadius, "skip, span 2, wrap");
        order.add(checkAutoOuterRadius);
        updateCheckboxAutoAftRadius();

        ////  Inner diameter
        panel.add(new JLabel(trans.get("BodyTubecfg.lbl.Innerdiameter")));

        // Diameter = 2*Radius
        final DoubleModel innerRadiusModel = new DoubleModel(component, "InnerRadius", 2, UnitGroup.UNITS_LENGTH, 0);
        register(innerRadiusModel);
        spin = new JSpinner(innerRadiusModel.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(innerRadiusModel), "growx");
        panel.add(new BasicSlider(innerRadiusModel.getSliderModel(new DoubleModel(0), od)), "w 100lp, wrap");


        ////  Wall thickness
        panel.add(new JLabel(trans.get("BodyTubecfg.lbl.Wallthickness")));

        final DoubleModel thicknessModel = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
        register(thicknessModel);
        spin = new JSpinner(thicknessModel.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(thicknessModel), "growx");
        panel.add(new BasicSlider(thicknessModel.getSliderModel(0, 0.01)), "w 100lp, wrap 0px");

        //// Filled
        BooleanModel bm = new BooleanModel(component, "Filled");
        register(bm);
        JCheckBox check = new JCheckBox(bm);
        check.setText(trans.get("BodyTubecfg.checkbox.Filled"));
        check.setToolTipText(trans.get("BodyTubecfg.checkbox.Filled.ttip"));
        panel.add(check, "skip, span 2, wrap");
        order.add(check);

        {//// CG calculation demonstration
            panel.add(new JLabel(trans.get("BodyTube.lbl.CgCalc") + ":"));
            JButton button = new JButton(trans.get("BodyTube.lbl.CgEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("BodyTube.lbl.CgCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.BodyTubeCgRequest request = new BodyTubeCgRequest(component.getLength());
                request.setAnswer(component.getComponentCG().x);
                String labelText = trans.get("BodyTube.lbl.length") + ": " + request.getLength();
                String constraints = "newline, height 30!";
                dialog.add(new JLabel(labelText), constraints);

                JButton checkButton = new JButton(trans.get("BodyTube.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("BodyTube.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("BodyTube.lbl.answer") + ": ");
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
                                checkResult.setText(trans.get("BodyTube.lbl.checkResult") + ": " + result.getResult());

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
            panel.add(new JLabel(trans.get("BodyTube.lbl.CpCalc") + ":"));
            JButton button = new JButton(trans.get("BodyTube.lbl.CpEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("BodyTube.lbl.CpCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.BodyTubeCpRequest request = new net.sf.openrocket.utils.educoder.BodyTubeCpRequest();
                request.setLength(c.getLength());
                request.setDivisions(128);
                try {
                    Method method1 = BodyTube.class.getDeclaredMethod("getOuterRadius");
                    method1.setAccessible(true);
                    Double radius = (Double) method1.invoke(component);
                    request.setOuterRadius(radius);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                FlightConfiguration curConfig = document.getSelectedConfiguration();
                FlightConditions conditions = new FlightConditions(curConfig);
                String[] methodNames = {"getSincAOA", "getSinAOA", "getRefArea", "getMach", "getAOA"};

                SymmetricComponentCalc componentCalc = new SymmetricComponentCalc(component);
                String[] fieldNames = {"planformCenter", "planformArea"};

                AerodynamicForces forces = new AerodynamicForces().zero();
                componentCalc.calculateNonaxialForces(conditions, new Transformation(0, 0, 0), forces, new WarningSet());
                request.setAnswer(forces.getCP().x);

                try {
                    for (String fieldName : fieldNames) {
                        Field field = SymmetricComponentCalc.class.getDeclaredField(fieldName);
                        Field reqField = net.sf.openrocket.utils.educoder.BodyTubeCpRequest.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        reqField.setAccessible(true);
                        Double value = (Double) field.get(componentCalc); // All values are double type
                        reqField.set(request, value);
                        String labelText = trans.get("BodyTube.lbl." + fieldName) + ": " + value;
                        String constraints = (fieldName.equals(fieldNames[0])) ? "spanx, height 30!" : "newline, height 30!";
                        dialog.add(new JLabel(labelText), constraints);
                    }
                    for (String methodName : methodNames) {
                        Method method = FlightConditions.class.getDeclaredMethod(methodName);
                        Method reqMethod = net.sf.openrocket.utils.educoder.BodyTubeCpRequest.class.getDeclaredMethod(methodName.replaceFirst("get", "set"), Double.class);
                        Double value = (Double) method.invoke(conditions); // All values are double type
                        reqMethod.invoke(request, value);
                        String labelText = trans.get("BodyTube.lbl." + methodName.replaceFirst("get", "")) + ": " + value;
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }

                    JButton checkButton = new JButton(trans.get("BodyTube.lbl.check"));
                    JLabel checkResult = new JLabel(trans.get("BodyTube.lbl.checkResult") + ": ");
                    JLabel answerLabel = new JLabel(trans.get("BodyTube.lbl.answer") + ": ");
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
                                    checkResult.setText(trans.get("BodyTube.lbl.checkResult") + ": " + result.getResult());

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
                } catch (Exception ex) {
                    // ignored
                }
                dialog.setVisible(true);
            });
        }

        { // MOI calculate:
            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.MOICalc") + ":"));
            JButton button = new JButton(trans.get("NoseConeCfg.lbl.MOIEnter"));
            panel.add(button, "spanx, wrap");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("NoseConeCfg.lbl.MOICalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.BodyTubeMOIRequest request = new net.sf.openrocket.utils.educoder.BodyTubeMOIRequest();
                // answer = rotationalUnitInertia
                request.setAnswer(new Double[]{component.getRotationalUnitInertia(), component.getLongitudinalUnitInertia()});

                String[] MethodNames = {"getOuterRadius", "getInnerRadius"};
                String[] fieldNames = {"thickness", "filled"};
                request.setLength(component.getLength());

                try {
                    //get and set  properties
                    for (String fieldName : fieldNames) {
                        Field field = SymmetricComponent.class.getDeclaredField(fieldName);
                        Field reqField = net.sf.openrocket.utils.educoder.BodyTubeMOIRequest.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        reqField.setAccessible(true);
                        Object value = field.get(component);
                        reqField.set(request, value);
                        String labelText = trans.get("NoseConeCfg.lbl." + fieldName) + ": " + value;
                        String constraints = (fieldName.equals(fieldNames[0])) ? "spanx, height 30!" : "newline, height 30!";
                        dialog.add(new JLabel(labelText), constraints);
                    }
                    // AftRadius
                    for (String methodName : MethodNames) {
                        Method method = BodyTube.class.getDeclaredMethod(methodName);
                        Method reqMethod = net.sf.openrocket.utils.educoder.BodyTubeMOIRequest.class.getDeclaredMethod(methodName.replaceFirst("get", "set"), Double.class);
                        Double value = (Double) method.invoke(component); // All values are double type
                        reqMethod.invoke(request, value);
                        String labelText = methodName.replaceFirst("get", "") + ": " + value;
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }
                    JButton checkButton = new JButton(trans.get("NoseConeCfg.lbl.check"));
                    JLabel checkResult = new JLabel(trans.get("NoseConeCfg.lbl.checkResult") + ": ");
                    JLabel answerLabel = new JLabel(trans.get("NoseConeCfg.lbl.answer") + ": ");
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
                                    checkResult.setText(trans.get("NoseConeCfg.lbl.checkResult") + ": " + result.getResult()[0] + "," + result.getResult()[1]);

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
        //// Material
        MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
        register(materialPanel);
        panel.add(materialPanel, "cell 4 0, gapleft 40lp, aligny 0%, spany");

        //// General and General properties
        tabbedPane.insertTab(trans.get("BodyTubecfg.tab.General"), null, panel,
                trans.get("BodyTubecfg.tab.Generalproperties"), 0);

        tabbedPane.setSelectedIndex(0);

        MotorConfig motorConfig = new MotorConfig((MotorMount) c, order);
        register(motorConfig);

        tabbedPane.insertTab(trans.get("BodyTubecfg.tab.Motor"), null, motorConfig,
                trans.get("BodyTubecfg.tab.Motormountconf"), 1);

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

    /**
     * Sets the checkAutoOuterRadius checkbox's enabled state and tooltip text, based on the state of its previous
     * component. If there is no next and previous symmetric component, the checkAutoOuterRadius checkbox is disabled.
     * If there is still a next or previous component which does not have its auto state enabled, meaning it can still
     * serve as a reference component for this component, the auto checkbox is enabled.
     */
    private void updateCheckboxAutoAftRadius() {
        if (component == null || checkAutoOuterRadius == null) return;

        // Disable check button if there is no component to get the diameter from
        SymmetricComponent prevComp = ((BodyTube) component).getPreviousSymmetricComponent();
        SymmetricComponent nextComp = ((BodyTube) component).getNextSymmetricComponent();
        if (prevComp == null && nextComp == null) {
            checkAutoOuterRadius.setEnabled(false);
            ((BodyTube) component).setOuterRadiusAutomatic(false);
            checkAutoOuterRadius.setToolTipText(trans.get("BodyTubecfg.checkbox.ttip.Automatic_noReferenceComponent"));
            return;
        }
        if (!(prevComp != null && nextComp == null && prevComp.usesNextCompAutomatic()) &&
                !(nextComp != null && prevComp == null && nextComp.usesPreviousCompAutomatic()) &&
                !(nextComp != null && prevComp != null && prevComp.usesNextCompAutomatic() && nextComp.usesPreviousCompAutomatic())) {
            checkAutoOuterRadius.setEnabled(true);
            checkAutoOuterRadius.setToolTipText(trans.get("BodyTubecfg.checkbox.ttip.Automatic"));
        } else {
            checkAutoOuterRadius.setEnabled(false);
            ((BodyTube) component).setOuterRadiusAutomatic(false);
            checkAutoOuterRadius.setToolTipText(trans.get("BodyTubecfg.checkbox.ttip.Automatic_alreadyAuto"));
        }
    }
}
