package net.sf.openrocket.gui.configdialog;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.rocketcomponent.ComponentAssembly;
import net.sf.openrocket.rocketcomponent.ParallelStage;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.RadiusMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.OpenRocket;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.utils.educoder.Result;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import static net.sf.openrocket.gui.configdialog.NoseConeConfig.roundToFiveDecimals;


@SuppressWarnings("serial")
public class ComponentAssemblyConfig extends RocketComponentConfig {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(ComponentAssemblyConfig.class);
    private final RocketComponent component;

    private JButton split = null;

    public ComponentAssemblyConfig(OpenRocketDocument document, RocketComponent component, JDialog parent) {
        super(document, component, parent);
        this.component = component;

        // only stages which are actually off-centerline will get the dialog here:
        if (ParallelStage.class.isAssignableFrom(component.getClass()) || PodSet.class.isAssignableFrom(component.getClass())) {
            tabbedPane.insertTab(trans.get("RocketCompCfg.tab.Assembly"), null, parallelTab((ComponentAssembly) component),
                    trans.get("RocketCompCfg.tab.AssemblyComment"), 0);
            tabbedPane.setSelectedIndex(0);

            addSplitButton();
        }
    }


    private JPanel parallelTab(final ComponentAssembly boosters) {
        JPanel motherPanel = new JPanel(new MigLayout("fillx"));

        // radial distance method
        JLabel radiusMethodLabel = new JLabel(trans.get("RocketComponent.Position.Method.Radius.Label"));
        motherPanel.add(radiusMethodLabel, "align left");
        final EnumModel<RadiusMethod> radiusMethodModel = new EnumModel<>(boosters, "RadiusMethod", RadiusMethod.choices());
        register(radiusMethodModel);
        final JComboBox<RadiusMethod> radiusMethodCombo = new JComboBox<>(radiusMethodModel);
        motherPanel.add(radiusMethodCombo, "spanx 3, wrap");
        order.add(radiusMethodCombo);

        // set radial distance
        JLabel radiusLabel = new JLabel(trans.get("ComponentAssemblyConfig.parallel.radius"));
        motherPanel.add(radiusLabel, "align left");
        //radiusMethodModel.addEnableComponent(radiusLabel, false);
        DoubleModel radiusModel = new DoubleModel(boosters, "RadiusOffset", UnitGroup.UNITS_LENGTH, 0);
        register(radiusModel);

        JSpinner radiusSpinner = new JSpinner(radiusModel.getSpinnerModel());
        radiusSpinner.setEditor(new SpinnerEditor(radiusSpinner));
        motherPanel.add(radiusSpinner, "wmin 65lp, growx 1, align right");
        order.add(((SpinnerEditor) radiusSpinner.getEditor()).getTextField());
//		autoRadOffsModel.addEnableComponent(radiusSpinner, false);
        UnitSelector radiusUnitSelector = new UnitSelector(radiusModel);
        motherPanel.add(radiusUnitSelector);
        motherPanel.add(new BasicSlider(radiusModel.getSliderModel(0, new DoubleModel(component.getParent(), "OuterRadius", 4.0, UnitGroup.UNITS_LENGTH))),
                "gapleft para, growx 2, wrap");

        radiusMethodCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                radiusModel.stateChanged(new EventObject(e));
            }
        });

        // set angle
        JLabel angleLabel = new JLabel(trans.get("ComponentAssemblyConfig.parallel.angle"));
        motherPanel.add(angleLabel, "align left");
        DoubleModel angleModel = new DoubleModel(boosters, "AngleOffset", 1.0, UnitGroup.UNITS_ANGLE, -Math.PI, Math.PI);
        register(angleModel);

        JSpinner angleSpinner = new JSpinner(angleModel.getSpinnerModel());
        angleSpinner.setEditor(new SpinnerEditor(angleSpinner));
        motherPanel.add(angleSpinner, "wmin 65lp, growx 1");
        order.add(((SpinnerEditor) angleSpinner.getEditor()).getTextField());
        UnitSelector angleUnitSelector = new UnitSelector(angleModel);
        motherPanel.add(angleUnitSelector);
        motherPanel.add(new BasicSlider(angleModel.getSliderModel(-Math.PI, Math.PI)), "gapleft para, growx 2, wrap");

        // set multiplicity
        JLabel countLabel = new JLabel(trans.get("ComponentAssemblyConfig.parallel.count"));
        motherPanel.add(countLabel, "align left");

        IntegerModel countModel = new IntegerModel(boosters, "InstanceCount", 1);
        register(countModel);
        JSpinner countSpinner = new JSpinner(countModel.getSpinnerModel());
        countSpinner.setEditor(new SpinnerEditor(countSpinner));
        motherPanel.add(countSpinner, "wmin 65lp, growx 1, wrap 30lp");
        order.add(((SpinnerEditor) countSpinner.getEditor()).getTextField());


        // Position relative to
        PlacementPanel pp = new PlacementPanel(component, order);
        register(pp);
        motherPanel.add(pp, "span, grow, wrap");

        {//// CG calculation demonstration
            motherPanel.add(new JLabel(trans.get("common.lbl.CgCalc") + ":"), "alignx left");
            JButton button2 = new JButton(trans.get("common.lbl.CgEnter"));
            motherPanel.add(button2, "spanx, wrap");
            button2.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("common.lbl.CgCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.PodsCgRequest request = new net.sf.openrocket.utils.educoder.PodsCgRequest();
                request.setAnswer(component.getComponentCG().x);

                JButton checkButton = new JButton(trans.get("common.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("common.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("common.lbl.answer") + ": ");
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
                                checkResult.setText(trans.get("common.lbl.checkResult") + ": " + result.getResult());

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
            motherPanel.add(new JLabel(trans.get("common.lbl.CpCalc") + ":"));
            JButton button2 = new JButton(trans.get("common.lbl.CpEnter"));
            motherPanel.add(button2, "spanx, wrap");
            button2.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("common.lbl.CpCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.PodsCpRequest request = new net.sf.openrocket.utils.educoder.PodsCpRequest();

                request.setAnswer(0.0);

                try {

                    JButton checkButton = new JButton(trans.get("common.lbl.check"));
                    JLabel checkResult = new JLabel(trans.get("common.lbl.checkResult") + ": ");
                    JLabel answerLabel = new JLabel(trans.get("common.lbl.answer") + ": ");
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
                                    checkResult.setText(trans.get("common.lbl.checkResult") + ": " + result.getResult());

                                    String msg = "";
                                    double answer =0.0;
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
                }
                dialog.setVisible(true);
            });
        }

        {//// MOI calculation demonstration
            motherPanel.add(new JLabel(trans.get("common.lbl.MOICal") + ":"), "alignx left");
            JButton button2 = new JButton(trans.get("common.lbl.MOIEnter"));
            motherPanel.add(button2, "spanx, wrap");
            button2.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("common.lbl.MOICal"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.PodsMOIRequest request = new net.sf.openrocket.utils.educoder.PodsMOIRequest();
                request.setAnswer(new Double[]{component.getRotationalUnitInertia(), component.getLongitudinalUnitInertia()});

                JButton checkButton = new JButton(trans.get("common.lbl.check"));
                JLabel checkResult = new JLabel(trans.get("common.lbl.checkResult") + ": ");
                JLabel answerLabel = new JLabel(trans.get("common.lbl.answer") + ": ");
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

        return motherPanel;
    }

    @Override
    public void updateFields() {
        super.updateFields();
        if (split != null) {
            split.setEnabled(component.getInstanceCount() > 1);
        }
    }

    private void addSplitButton() {
        //// Split fins
        final String btnText;
        final String btnTextTtip;
        final boolean freezeRocket;
        if (PodSet.class.isAssignableFrom(component.getClass())) {
            btnText = trans.get("ComponentAssemblyConfig.but.splitPods");
            btnTextTtip = trans.get("ComponentAssemblyConfig.but.splitPods.ttip");
            freezeRocket = true;
        } else if (ParallelStage.class.isAssignableFrom(component.getClass())) {
            btnText = trans.get("ComponentAssemblyConfig.but.splitBoosters");
            btnTextTtip = trans.get("ComponentAssemblyConfig.but.splitBoosters.ttip");
            freezeRocket = false;
        } else {
            return;
        }
        split = new SelectColorButton(btnText);
        split.setToolTipText(btnTextTtip);
        split.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info(Markers.USER_MARKER, "Splitting " + component.getComponentName() + " into separate assemblies, instance count=" +
                        component.getInstanceCount());

                // This is a bit awkward, we need to store the listeners before closing the dialog, because closing it
                // will remove them. We then add them back before the split and remove them afterwards.
                List<RocketComponent> listeners = new ArrayList<>(component.getConfigListeners());


                // Do change in future for overall safety
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        disposeDialog();

                        document.startUndo("Split assembly");
                        for (RocketComponent listener : listeners) {
                            component.addConfigListener(listener);
                        }
                        component.splitInstances(freezeRocket);
                        component.clearConfigListeners();
                        document.stopUndo();
                    }
                });
            }
        });
        split.setEnabled(component.getInstanceCount() > 1);

        addButtons(split);
        order.add(split);
    }
}
