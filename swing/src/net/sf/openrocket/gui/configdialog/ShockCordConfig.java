package net.sf.openrocket.gui.configdialog;


import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.CustomFocusTraversalPolicy;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.rocketcomponent.MassObject;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.OpenRocket;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.utils.educoder.Result;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import static net.sf.openrocket.gui.configdialog.NoseConeConfig.roundToFiveDecimals;

@SuppressWarnings("serial")
public class ShockCordConfig extends RocketComponentConfig {
    private static final Translator trans = Application.getTranslator();

    public ShockCordConfig(OpenRocketDocument d, RocketComponent component, JDialog parent) {
        super(d, component, parent);

        JPanel primary = new JPanel(new MigLayout());

        //////  Left side
        JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));
        primary.add(panel, "grow");
        JLabel label;
        DoubleModel m;
        JSpinner spin;

        //	Attributes

        //// Shock cord length
        label = new JLabel(trans.get("ShockCordCfg.lbl.Shockcordlength"));
        panel.add(label);

        m = new DoubleModel(component, "CordLength", UnitGroup.UNITS_LENGTH, 0);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(0, 1, 10)), "w 100lp, wrap");

        // Material
        //// Shock cord material:
        MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.LINE,
                trans.get("ShockCordCfg.lbl.Shockcordmaterial"), null, "Material", order);
        register(materialPanel);
        panel.add(materialPanel, "spanx 4, wrap, gapright 40lp");

        {//// CG calculation demonstration
            panel.add(new JLabel(trans.get("Parachute.lbl.CgCalc") + ":"), "alignx left");
            JButton button2 = new JButton(trans.get("Parachute.lbl.CgEnter"));
            panel.add(button2, "spanx, wrap");
            button2.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("Parachute.lbl.CgCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.ShockCordCgRequest request = new net.sf.openrocket.utils.educoder.ShockCordCgRequest();
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

                final net.sf.openrocket.utils.educoder.ShockCordMOIRequest request = new net.sf.openrocket.utils.educoder.ShockCordMOIRequest();
                request.setAnswer(new Double[]{component.getRotationalUnitInertia(), component.getLongitudinalUnitInertia()});
                request.setLength(component.getLength());

                String[] methodNames = {"getRadius"};
                try {
                    for (String methodName : methodNames) {
                        Method method = MassObject.class.getDeclaredMethod(methodName);
                        Method reqMethod = net.sf.openrocket.utils.educoder.ShockCordMOIRequest.class.getDeclaredMethod(methodName.replaceFirst("get", "set"), Double.class);
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
                dialog.setVisible(true);
            });
        }
        /////  Right side
        panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));
        primary.add(panel, "aligny 0%, grow, spany");

        { // ----------- Placement ----------
            //// Position relative to:
            PlacementPanel placementPanel = new PlacementPanel(component, order);
            register(placementPanel);
            panel.add(placementPanel, "span, grow, wrap");

            {//// Packed length:
                placementPanel.add(new JLabel(trans.get("ShockCordCfg.lbl.Packedlength")), "newline");

                m = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
                register(m);

                spin = new JSpinner(m.getSpinnerModel());
                spin.setEditor(new SpinnerEditor(spin));
                placementPanel.add(spin, "growx");
                order.add(((SpinnerEditor) spin.getEditor()).getTextField());

                placementPanel.add(new UnitSelector(m), "growx");
                placementPanel.add(new BasicSlider(m.getSliderModel(0, 0.1, 0.5)), "w 100lp, wrap");
            }


            {//// Packed diameter:
                placementPanel.add(new JLabel(trans.get("ShockCordCfg.lbl.Packeddiam")));

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
                placementPanel.add(checkAutoPackedRadius, "skip, spanx 2, wrap");
                order.add(checkAutoPackedRadius);
            }
        }


        //// General and General properties
        tabbedPane.insertTab(trans.get("ShockCordCfg.tab.General"), null, primary,
                trans.get("ShockCordCfg.tab.ttip.General"), 0);
        //// Radial position and Radial position configuration
        tabbedPane.insertTab(trans.get("ShockCordCfg.tab.Radialpos"), null, positionTab(),
                trans.get("ShockCordCfg.tab.ttip.Radialpos"), 1);
        tabbedPane.setSelectedIndex(0);

        // Apply the custom focus travel policy to this config dialog
        //// Make sure the cancel & ok button is the last component
        order.add(cancelButton);
        order.add(okButton);
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
        parent.setFocusTraversalPolicy(policy);
    }

    // TODO: LOW: there is a lot of duplicate code here with other mass components... (e.g. in MassComponentConfig or ParachuteConfig)
    protected JPanel positionTab() {
        JPanel panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));

        ////  Radial position
        //// Radial distance:
        panel.add(new JLabel(trans.get("ShockCordCfg.lbl.Radialdistance")));

        DoubleModel m = new DoubleModel(component, "RadialPosition", UnitGroup.UNITS_LENGTH, 0);
        register(m);

        JSpinner spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(0, 0.1, 1.0)), "w 150lp, wrap");


        //// Radial direction:
        panel.add(new JLabel(trans.get("ShockCordCfg.lbl.Radialdirection")));

        m = new DoubleModel(component, "RadialDirection", UnitGroup.UNITS_ANGLE);
        register(m);

        spin = new JSpinner(m.getSpinnerModel());
        spin.setEditor(new SpinnerEditor(spin));
        panel.add(spin, "growx");
        order.add(((SpinnerEditor) spin.getEditor()).getTextField());

        panel.add(new UnitSelector(m), "growx");
        panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 150lp, wrap");


        //// Reset button
        JButton button = new SelectColorButton(trans.get("ShockCordCfg.but.Reset"));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((ShockCord) component).setRadialDirection(0.0);
                ((ShockCord) component).setRadialPosition(0.0);
            }
        });
        panel.add(button, "spanx, right");
        order.add(button);

        return panel;
    }
}
