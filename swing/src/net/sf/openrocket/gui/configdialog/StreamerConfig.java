package net.sf.openrocket.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.CustomFocusTraversalPolicy;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.MaterialModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.HtmlLabel;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
import net.sf.openrocket.rocketcomponent.DeploymentConfiguration.DeployEvent;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.OpenRocket;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.utils.educoder.Result;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.sf.openrocket.gui.configdialog.NoseConeConfig.roundToFiveDecimals;

public class StreamerConfig extends RecoveryDeviceConfig {
    private static final long serialVersionUID = -4445736703470494588L;
    private static final Translator trans = Application.getTranslator();

    public StreamerConfig(OpenRocketDocument d, final RocketComponent component, JDialog parent) {
        super(d, component, parent);
        Streamer streamer = (Streamer) component;

        JPanel primary = new JPanel(new MigLayout());

        //	Left side
        JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::][]"));

        //// ---------------------------- Attributes ----------------------------

        //// Strip length:
        panel.add(new JLabel(trans.get("StreamerCfg.lbl.Striplength")));

        DoubleModel m = new DoubleModel(component, "StripLength", UnitGroup.UNITS_LENGTH, 0);
        register(m);

        JSpinner spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());
        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(0, 0.6, 1.5)), "w 150lp, wrap");

        //// Strip width:
        panel.add(new JLabel(trans.get("StreamerCfg.lbl.Stripwidth")));

        m = new DoubleModel(component, "StripWidth", UnitGroup.UNITS_LENGTH, 0);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());
        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(0, 0.2)), "w 150lp, wrap 10lp");

        //// Strip area:
        panel.add(new JLabel(trans.get("StreamerCfg.lbl.Striparea")));

        m = new DoubleModel(component, "Area", UnitGroup.UNITS_AREA, 0);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());
        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(0, 0.04, 0.25)), "w 150lp, wrap");

        //// Aspect ratio:
        panel.add(new JLabel(trans.get("StreamerCfg.lbl.Aspectratio")));

        m = new DoubleModel(component, "AspectRatio", UnitGroup.UNITS_NONE, 0);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());
        //		panel.add(new UnitSelector(m),"growx");
        panel.add(new BasicSlider(m.getSliderModel(2, 15)), "skip, w 150lp, wrap 10lp");

        //// Material:
        panel.add(new JLabel(trans.get("StreamerCfg.lbl.Material")));

        MaterialModel mm = new MaterialModel(panel, component, Material.Type.SURFACE);
        register(mm);
        JComboBox<Material> streamerMaterialCombo = new JComboBox<>(mm);
        //// The component material affects the weight of the component.
        streamerMaterialCombo.setToolTipText(trans.get("StreamerCfg.combo.ttip.MaterialModel"));
        panel.add(streamerMaterialCombo, "spanx 3, growx, wrap 15lp");
        order.add(streamerMaterialCombo);

        // CD
        //// <html>Drag coefficient C<sub>D</sub>:
        JLabel label = new HtmlLabel(trans.get("StreamerCfg.lbl.longA1"));
        //// <html>The drag coefficient relative to the total area of the streamer.<br>
        String tip = trans.get("StreamerCfg.lbl.longB1") +
                //// "A larger drag coefficient yields a slowed descent rate.
                trans.get("StreamerCfg.lbl.longB2");
        label.setToolTipText(tip);
        panel.add(label);

        m = new DoubleModel(component, "CD", UnitGroup.UNITS_COEFFICIENT, 0);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setToolTipText(tip);
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        JCheckBox check = new JCheckBox(m.getAutomaticAction());
        //// Automatic
        check.setText(trans.get("StreamerCfg.lbl.AutomaticCd"));
        check.setToolTipText(trans.get("StreamerCfg.lbl.AutomaticCd.ttip"));
        panel.add(check, "skip, span, wrap");
        order.add(check);

        //// The drag coefficient is relative to the area of the streamer.
        panel.add(new StyledLabel(trans.get("StreamerCfg.lbl.longC1"),
                -1, StyledLabel.Style.ITALIC), "gapleft para, span, wrap");

        primary.add(panel, "grow, gapright 20lp");

        {//// CG calculation demonstration
            panel.add(new JLabel(trans.get("Parachute.lbl.CgCalc") + ":"), "alignx left");
            JButton button2 = new JButton(trans.get("Parachute.lbl.CgEnter"));
            panel.add(button2, "spanx, wrap");
            button2.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("Parachute.lbl.CgCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.StreamerCgRequest request = new net.sf.openrocket.utils.educoder.StreamerCgRequest();
                request.setAnswer(component.getComponentCG().x);
                request.setLength(component.getLength());
                String labelText = trans.get("Parachute.lbl.length") + ": " + request.getLength();
                String constraints = "newline, height 30!";
                dialog.add(new JLabel(labelText), constraints);

                JButton checkButton = new JButton(trans.get("Parachute.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("InnerTube.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("InnerTube.lbl.answer") + ": ");
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
                                checkResult.setText(trans.get("InnerTube.lbl.checkResult") + ": " + result.getResult());
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
                    public void onFailure(@NotNull Call<Result> call, @NotNull Throwable throwable) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(parent, throwable.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
                    }
                }));
                dialog.setVisible(true);
            });
        }

        {//// MOI calculation demonstration
            panel.add(new JLabel(trans.get("Parachute.lbl.MOICal") + ":"), "alignx left");
            JButton button2 = new JButton(trans.get("Parachute.lbl.MOIEnter"));
            panel.add(button2, "spanx, wrap");
            button2.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("Parachute.lbl.MOICal"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.StreamerMOIRequest request = new net.sf.openrocket.utils.educoder.StreamerMOIRequest();
                request.setAnswer(new Double[]{component.getRotationalUnitInertia(), component.getLongitudinalUnitInertia()});
                request.setLength(component.getLength());

                String[] methodNames = {"getRadius"};
                try {
                    for (String methodName : methodNames) {
                        Method method = MassObject.class.getDeclaredMethod(methodName);
                        Method reqMethod = net.sf.openrocket.utils.educoder.StreamerMOIRequest.class.getDeclaredMethod(methodName.replaceFirst("get", "set"), Double.class);
                        method.setAccessible(true);
                        reqMethod.setAccessible(true);
                        Double value = (Double) method.invoke(component);
                        reqMethod.invoke(request, value);
                        String labelText = trans.get("Parachute.lbl." + methodName.replaceFirst("get", "")) + ": " + value;
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }


                JButton checkButton = new JButton(trans.get("Parachute.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("Parachute.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("Parachute.lbl.answer") + ": ");
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
                                answerLabel.setText(trans.get("NoseConeCfg.lbl.answer") + ": " + component.getRotationalUnitInertia() + "," + component.getLongitudinalUnitInertia());
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

        //	Right side
        panel = new JPanel(new MigLayout("ins 0"));


        {//// ---------------------------- Placement ----------------------------
            PlacementPanel placementPanel = new PlacementPanel(component, order);
            register(placementPanel);

            ////  Packed length:
            placementPanel.add(new JLabel(trans.get("StreamerCfg.lbl.Packedlength")), "newline");

            m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
            register(m);

            spin = new JSpinner(m.getSpinnerModel());
            spin.setEditor(new SpinnerEditor(spin));
            placementPanel.add(spin, "growx");
            order.add(((SpinnerEditor) spin.getEditor()).getTextField());

            placementPanel.add(new UnitSelector(m), "growx");
            placementPanel.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.5)), "w 100lp, wrap");

            //// Packed diameter:
            placementPanel.add(new JLabel(trans.get("StreamerCfg.lbl.Packeddiam")));

            DoubleModel od = new DoubleModel(component, "Radius", 2, UnitGroup.UNITS_LENGTH, 0);
            register(od);
            spin = new JSpinner(od.getSpinnerModel());
            spin.setEditor(new SpinnerEditor(spin));
            placementPanel.add(spin, "growx");
            order.add(((SpinnerEditor) spin.getEditor()).getTextField());

            placementPanel.add(new UnitSelector(od), "growx");
            placementPanel.add(new BasicSlider(od.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap");

            ////// Automatic
            JCheckBox checkAutoPackedRadius = new JCheckBox(od.getAutomaticAction());
            checkAutoPackedRadius.setText(trans.get("ParachuteCfg.checkbox.AutomaticPacked"));
            checkAutoPackedRadius.setToolTipText(trans.get("ParachuteCfg.checkbox.AutomaticPacked.ttip"));
            placementPanel.add(checkAutoPackedRadius, "skip, spanx 2");
            order.add(checkAutoPackedRadius);

            panel.add(placementPanel, "growx, wrap");
        }

        //// ---------------------------- Deployment ----------------------------
        JPanel deploymentPanel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::][]"));
        deploymentPanel.setBorder(BorderFactory.createTitledBorder(trans.get("StreamerCfg.lbl.Deployment")));

        //// Deploys at:
        deploymentPanel.add(new JLabel(trans.get("StreamerCfg.lbl.Deploysat") + " " + CommonStrings.dagger), "");

        DeploymentConfiguration deploymentConfig = streamer.getDeploymentConfigurations().getDefault();
        EnumModel<DeploymentConfiguration.DeployEvent> em = new EnumModel<>(deploymentConfig, "DeployEvent");
        register(em);
        JComboBox<DeploymentConfiguration.DeployEvent> eventCombo = new JComboBox<>(em);
        if ((component.getStageNumber() + 1) == d.getRocket().getStageCount()) {
            //	This is the bottom stage.  restrict deployment options.
            eventCombo.removeItem(DeployEvent.LOWER_STAGE_SEPARATION);
        }
        eventCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateFields();
            }
        });
        deploymentPanel.add(eventCombo, "spanx 3, growx, wrap");
        order.add(eventCombo);

        // ... and delay
        //// plus
        deploymentPanel.add(new JLabel(trans.get("StreamerCfg.lbl.plusdelay")), "right");

        m = new DoubleModel(deploymentConfig, "DeployDelay", 0);
        register(m);
        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin, 3));
        deploymentPanel.add(spin, "spanx, split");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        //// seconds
        deploymentPanel.add(new JLabel(trans.get("StreamerCfg.lbl.seconds")), "wrap paragraph");

        // Altitude:
        label = new JLabel(trans.get("StreamerCfg.lbl.Altitude") + CommonStrings.dagger);
        altitudeComponents.add(label);
        deploymentPanel.add(label);

        m = new DoubleModel(deploymentConfig, "DeployAltitude", UnitGroup.UNITS_DISTANCE, 0);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        altitudeComponents.add(spin);
        deploymentPanel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());
        UnitSelector unit = new UnitSelector(m);
        altitudeComponents.add(unit);
        deploymentPanel.add(unit, "growx");
        BasicSlider slider = new BasicSlider(m.getSliderModel(100, 1000));
        altitudeComponents.add(slider);
        deploymentPanel.add(slider, "w 100lp, wrap");

        deploymentPanel.add(new StyledLabel(CommonStrings.override_description, -1), "spanx, wrap");

        panel.add(deploymentPanel, "growx");
        primary.add(panel, "grow");

        updateFields();

        //// General and General properties
        tabbedPane.insertTab(trans.get("StreamerCfg.tab.General"), null, primary,
                trans.get("StreamerCfg.tab.ttip.General"), 0);
        //// Radial position and Radial position configuration
        tabbedPane.insertTab(trans.get("StreamerCfg.tab.Radialpos"), null, positionTab(),
                trans.get("StreamerCfg.tab.ttip.Radialpos"), 1);
        tabbedPane.setSelectedIndex(0);

        // Apply the custom focus travel policy to this config dialog
        //// Make sure the cancel & ok button is the last component
        order.add(cancelButton);
        order.add(okButton);
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
        parent.setFocusTraversalPolicy(policy);
    }


    protected JPanel positionTab() {
        JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));

        //// Radial position
        //// Radial distance:
        panel.add(new JLabel(trans.get("StreamerCfg.lbl.Radialdistance")));

        DoubleModel m = new DoubleModel(component, "RadialPosition", UnitGroup.UNITS_LENGTH, 0);
        register(m);

        JSpinner spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 150lp, wrap");

        //// Radial direction:
        panel.add(new JLabel(trans.get("StreamerCfg.lbl.Radialdirection")));

        m = new DoubleModel(component, "RadialDirection", UnitGroup.UNITS_ANGLE);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 150lp, wrap");


        //// Reset button
        JButton button = new SelectColorButton(trans.get("StreamerCfg.but.Reset"));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Streamer) component).setRadialDirection(0.0);
                ((Streamer) component).setRadialPosition(0.0);
            }
        });
        panel.add(button, "spanx, right");
        order.add(button);

        return panel;
    }
}
