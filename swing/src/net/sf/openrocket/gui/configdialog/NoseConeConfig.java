package net.sf.openrocket.gui.configdialog;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

import javax.swing.*;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.AerodynamicForces;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.barrowman.RocketComponentCalc;
import net.sf.openrocket.aerodynamics.barrowman.SymmetricComponentCalc;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.CustomFocusTraversalPolicy;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.TransitionShapeModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.masscalc.MassCalculation;
import net.sf.openrocket.masscalc.MassCalculator;
import net.sf.openrocket.masscalc.RigidBody;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.OpenRocket;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.Transformation;
import net.sf.openrocket.utils.educoder.NoseConeCgRequest;
import net.sf.openrocket.utils.educoder.WholeCpDTO;
import net.sf.openrocket.utils.educoder.WholeCpRequest;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("serial")
public class NoseConeConfig extends RocketComponentConfig {


    private DescriptionArea description;

    private JLabel shapeLabel;
    private JSpinner shapeSpinner;
    private JSlider shapeSlider;
    private final JCheckBox checkAutoBaseRadius;
    private static final Translator trans = Application.getTranslator();

    // Prepended to the description from NoseCone.DESCRIPTIONS
    private static final String PREDESC = "<html>";

    public NoseConeConfig(OpenRocketDocument d, RocketComponent c, JDialog parent) {
        super(d, c, parent);

        final JPanel panel = new JPanel(new MigLayout("", "[][65lp::][30lp::]"));

        ////  Shape selection
        {//// Nose cone shape:
            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.Noseconeshape")));

            TransitionShapeModel tm = new TransitionShapeModel(c);
            register(tm);
            final JComboBox<Transition.Shape> typeBox = new JComboBox<>(tm);
            typeBox.setEditable(false);
            typeBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Transition.Shape s = (Transition.Shape) typeBox.getSelectedItem();
                    if (s != null) {
                        description.setText(PREDESC + s.getNoseConeDescription());
                    }
                    updateEnabled();
                }
            });
            panel.add(typeBox, "spanx 3, growx, wrap rel");
            order.add(typeBox);

            ////  Shape parameter:
            this.shapeLabel = new JLabel(trans.get("NoseConeCfg.lbl.Shapeparam"));
            panel.add(shapeLabel);

            final DoubleModel parameterModel = new DoubleModel(component, "ShapeParameter", UnitGroup.UNITS_SHAPE_PARAMETER, 0, 1);
            register(parameterModel);

            this.shapeSpinner = new JSpinner(parameterModel.getSpinnerModel());
            shapeSpinner.setEditor(new SpinnerEditor(shapeSpinner));
            panel.add(shapeSpinner, "growx");
            order.add(((SpinnerEditor) shapeSpinner.getEditor()).getTextField());

            DoubleModel min = new DoubleModel(component, "ShapeParameterMin");
            DoubleModel max = new DoubleModel(component, "ShapeParameterMax");
            register(min);
            register(max);
            this.shapeSlider = new BasicSlider(parameterModel.getSliderModel(min, max));
            panel.add(shapeSlider, "skip, w 100lp, wrap para");

            updateEnabled();
        }

        { /// Nose cone length:
            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.Noseconelength")));

            final DoubleModel lengthModel = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
            register(lengthModel);
            JSpinner spin = new JSpinner(lengthModel.getSpinnerModel());
            spin.setEditor(new SpinnerEditor(spin));
            panel.add(spin, "growx");
            order.add(((SpinnerEditor) spin.getEditor()).getTextField());

            panel.add(new UnitSelector(lengthModel), "growx");
            panel.add(new BasicSlider(lengthModel.getSliderModel(0, 0.1, 0.7)), "w 100lp, wrap");
        }
        {
            /// Base diameter:

            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.Basediam")));

            final DoubleModel baseRadius = new DoubleModel(component, "BaseRadius", 2.0, UnitGroup.UNITS_LENGTH, 0); // Diameter = 2*Radius
            register(baseRadius);
            final JSpinner radiusSpinner = new JSpinner(baseRadius.getSpinnerModel());
            radiusSpinner.setEditor(new SpinnerEditor(radiusSpinner));
            panel.add(radiusSpinner, "growx");
            order.add(((SpinnerEditor) radiusSpinner.getEditor()).getTextField());

            panel.add(new UnitSelector(baseRadius), "growx");
            panel.add(new BasicSlider(baseRadius.getSliderModel(0, 0.04, 0.2)), "w 100lp, wrap 0px");

            checkAutoBaseRadius = new JCheckBox(baseRadius.getAutomaticAction());
            //// Automatic
            checkAutoBaseRadius.setText(trans.get("NoseConeCfg.checkbox.Automatic"));
            panel.add(checkAutoBaseRadius, "skip, span 2, wrap");
            order.add(checkAutoBaseRadius);
            updateCheckboxAutoBaseRadius(((NoseCone) component).isFlipped());
        }

        {////  Wall thickness:
            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.Wallthickness")));
            final DoubleModel thicknessModel = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
            register(thicknessModel);
            final JSpinner thicknessSpinner = new JSpinner(thicknessModel.getSpinnerModel());
            thicknessSpinner.setEditor(new SpinnerEditor(thicknessSpinner));
            panel.add(thicknessSpinner, "growx");
            order.add(((SpinnerEditor) thicknessSpinner.getEditor()).getTextField());

            panel.add(new UnitSelector(thicknessModel), "growx");
            panel.add(new BasicSlider(thicknessModel.getSliderModel(0,
                            new DoubleModel(component, "MaxRadius", UnitGroup.UNITS_LENGTH))),
                    "w 100lp, wrap 0px");


            BooleanModel bm = new BooleanModel(component, "Filled");
            register(bm);
            final JCheckBox filledCheckbox = new JCheckBox(bm);
            //// Filled
            filledCheckbox.setText(trans.get("NoseConeCfg.checkbox.Filled"));
            filledCheckbox.setToolTipText(trans.get("NoseConeCfg.checkbox.Filled.ttip"));
            panel.add(filledCheckbox, "skip, span 2, wrap para");
            order.add(filledCheckbox);
        }

        {
            //// CG calculation demonstration
            //// 头椎重心计算
            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.CgCalc") + ":"));
            JButton button = new JButton(trans.get("NoseConeCfg.lbl.CgEnter"));
            panel.add(button);
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("NoseConeCfg.lbl.CgCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final NoseConeCgRequest request = new NoseConeCgRequest();
                request.setAnswer(component.getComponentCG().x);

                String[] transitionMethodNames = {"getForeRadius", "getAftRadius"};
                String[] transitionFieldNames = {"shapeParameter", "type"};
                String[] fieldNames = {"filled", "thickness", "DIVISIONS"};

                try {
                    for (String fieldName : fieldNames) {
                        Field field = SymmetricComponent.class.getDeclaredField(fieldName);
                        Field reqField = NoseConeCgRequest.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        reqField.setAccessible(true);
                        Object value = field.get(component);
                        reqField.set(request, value);
                        String labelText = trans.get("NoseConeCfg.lbl." + fieldName) + ": " + value;
                        String constraints = (fieldName.equals(fieldNames[0])) ? "spanx, height 30!" : "newline, height 30!";
                        dialog.add(new JLabel(labelText), constraints);
                    }
                    // Component Length
                    double length = component.getLength();
                    request.setLength(length);
                    String lengthLabelText = trans.get("NoseConeCfg.lbl.length") + ": " + length;
                    dialog.add(new JLabel(lengthLabelText), "newline, height 30!");
                    // Material Density
                    double density = ((NoseCone) component).getMaterial().getDensity();
                    request.setDensity(density);
                    lengthLabelText = trans.get("NoseConeCfg.lbl.density") + ": " + density;
                    dialog.add(new JLabel(lengthLabelText), "newline, height 30!");

                    for (String fieldName : transitionFieldNames) {
                        Field field = Transition.class.getDeclaredField(fieldName);
                        String reqFieldName = "transition" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                        Field reqField = NoseConeCgRequest.class.getDeclaredField(reqFieldName);
                        field.setAccessible(true);
                        reqField.setAccessible(true);
                        Object value = field.get(component);
                        reqField.set(request, value);
                        String labelText = trans.get("NoseConeCfg.lbl." + reqFieldName) + ": " + value;
                        if (value instanceof Transition.Shape)
                            labelText = trans.get("NoseConeCfg.lbl." + reqFieldName) + ": " + ((Transition.Shape) value).name();
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }
                    request.setForeShoulderLength(((NoseCone) component).getForeShoulderLength());
                    request.setForeShoulderRadius(((NoseCone) component).getForeShoulderRadius());
                    request.setAftShoulderRadius(((NoseCone) component).getAftShoulderRadius());
                    request.setForeShoulderThickness(((NoseCone) component).getForeShoulderThickness());
                    request.setAftShoulderLength(((NoseCone) component).getAftShoulderLength());
                    request.setAftShoulderThickness(((NoseCone) component).getAftShoulderThickness());
                    request.setIsForeShoulderCapped(((NoseCone) component).isForeShoulderCapped());
                    request.setIsAftShoulderCapped(((NoseCone) component).isAftShoulderCapped());
                    for (String methodName : transitionMethodNames) {
                        Method method = Transition.class.getDeclaredMethod(methodName);
                        Method reqMethod = NoseConeCgRequest.class
                                .getDeclaredMethod(methodName.replaceFirst("get", "setTransition"), Double.class);
                        Double value = (Double) method.invoke(component); // All values are double type
                        reqMethod.invoke(request, value);
                        String labelText = trans.get("NoseConeCfg.lbl." + methodName.replaceFirst("get", "transition")) + ": " + value;
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }
                    JButton checkButton = new JButton(trans.get("NoseConeCfg.lbl.check"));
                    JLabel checkResult = new JLabel(trans.get("NoseConeCfg.lbl.checkResult") + ": ");
                    JLabel answerLabel = new JLabel(trans.get("NoseConeCfg.lbl.answer") + ": ");
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
        {//// whole CG calculation demonstration
            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.WholeCgCalc") + ":"), "gapleft 15px");
            JButton button = new JButton(trans.get("NoseConeCfg.lbl.WholeCgEnter"));
            panel.add(button, "span");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("NoseConeCfg.lbl.WholeCgCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));


                final net.sf.openrocket.utils.educoder.WholeCgRequest request = new net.sf.openrocket.utils.educoder.WholeCgRequest();

                try {
                    //whole cg answer
                    RocketComponent rocket = document.getSelectedConfiguration().getRocket();
                    net.sf.openrocket.utils.educoder.MyComponent rootComponent = new net.sf.openrocket.utils.educoder.MyComponent();
                    rootComponent.setComponentName(rocket.getComponentName());
                    rootComponent.setCg(rocket.getCG());
                    rootComponent.setPosition(rocket.getPosition());
                    rootComponent.copyValues(rocket, document.getSelectedConfiguration());

                    request.setMyComponent(rootComponent);

                    FlightConfiguration configuration = document.getSelectedConfiguration();
                    MassCalculation calculation = new MassCalculation(MassCalculation.Type.LAUNCH, configuration,
                            Motor.PSEUDO_TIME_LAUNCH, null, configuration.getRocket(), Transformation.IDENTITY, null);
                    Method method = MassCalculation.class.getDeclaredMethod("calculateAssembly");
                    method.setAccessible(true);
                    method.invoke(calculation);
                    request.setAnswer(calculation.getCM().x);

                    //计算interTube
                    int i = 1;
                    for (RocketComponent component : rocket.getAllChildren()) {
                        if (component.getComponentName().equals("火箭") || component.getComponentName().equals("火箭级"))
                            continue;
                        String labelText = component.getComponentName() + " 组件重心: " + component.getComponentCG().x;
                        String labelText2 = component.getComponentName() + "组件位置: " + component.getPosition();
                        String constraints = "newline, height 30!";
                        String constraints2 = "height 30!";
                        if (i < 11) {
                            dialog.add(new JLabel(labelText), constraints);
                            dialog.add(new JLabel(labelText2), constraints2);
                        }
                        i++;

                    }
                    JButton checkButton = new JButton(trans.get("NoseConeCfg.lbl.check"));
                    JLabel checkResult = new JLabel(trans.get("NoseConeCfg.lbl.checkResult") + ": ");
                    JLabel answerLabel = new JLabel(trans.get("NoseConeCfg.lbl.answer") + ": ");
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
                                    double answer = calculation.getCM().x;
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
                    ex.printStackTrace();

                }
                dialog.setVisible(true);
            });
        }
        {//// CP calculation demonstration
            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.CpCalc") + ":"));
            JButton button = new JButton(trans.get("NoseConeCfg.lbl.CpEnter"));
            panel.add(button);
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("NoseConeCfg.lbl.CpCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.NoseConeCpRequest request = new net.sf.openrocket.utils.educoder.NoseConeCpRequest();

                FlightConfiguration curConfig = document.getSelectedConfiguration();
                FlightConditions conditions = new FlightConditions(curConfig);

                String[] methodNames = {"getSincAOA", "getSinAOA", "getRefArea", "getMach", "getAOA"};

                SymmetricComponentCalc componentCalc = new SymmetricComponentCalc(component);
                String[] fieldNames = {"foreRadius", "aftRadius", "length", "fullVolume", "planformCenter", "planformArea"};
                AerodynamicForces forces2 = new AerodynamicForces().zero();

                Double[] aoa = arange(-90, 90, 1);
                List<Double> result = new ArrayList<>();
                for (Double eachAoA : aoa) {
                    conditions.setAOA(eachAoA * (Math.PI / 180));
                    componentCalc.calculateNonaxialForces(conditions, new Transformation(0, 0, 0), forces2, new WarningSet());
                    result.add(forces2.getCP().x);
                }
                AerodynamicForces forces = new AerodynamicForces().zero();
                componentCalc.calculateNonaxialForces(conditions, new Transformation(0, 0, 0), forces, new WarningSet());
                request.setAnswer(forces.getCP().x);

                try {
                    for (String fieldName : fieldNames) {
                        Field field = SymmetricComponentCalc.class.getDeclaredField(fieldName);
                        Field reqField = net.sf.openrocket.utils.educoder.NoseConeCpRequest.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        reqField.setAccessible(true);
                        Double value = (Double) field.get(componentCalc); // All values are double type
                        reqField.set(request, value);
                        String labelText = trans.get("NoseConeCfg.lbl." + fieldName) + ": " + value;
                        String constraints = (fieldName.equals(fieldNames[0])) ? "spanx, height 30!" : "newline, height 30!";
                        dialog.add(new JLabel(labelText), constraints);
                    }
                    for (String methodName : methodNames) {
                        Method method = FlightConditions.class.getDeclaredMethod(methodName);
                        Method reqMethod = net.sf.openrocket.utils.educoder.NoseConeCpRequest.class.getDeclaredMethod(methodName.replaceFirst("get", "set"), Double.class);
                        Double value = (Double) method.invoke(conditions); // All values are double type
                        reqMethod.invoke(request, value);
                        String labelText = trans.get("NoseConeCfg.lbl." + methodName.replaceFirst("get", "")) + ": " + value;
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }

                    JButton checkButton = new JButton(trans.get("NoseConeCfg.lbl.check"));
                    JLabel checkResult = new JLabel(trans.get("NoseConeCfg.lbl.checkResult") + ": ");
                    JLabel answerLabel = new JLabel(trans.get("NoseConeCfg.lbl.answer") + ": ");
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
                                    checkResult.setText(trans.get("NoseConeCfg.lbl.checkResult") + ": " + result.getResult());
                                    String msg = "";
                                    double answer = forces.getCP().x;
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
        {//// whole CP calculation demonstration
            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.WholeCpCalc") + ":"), "gapleft 15px");
            JButton button = new JButton(trans.get("NoseConeCfg.lbl.WholeCpEnter"));
            panel.add(button, "span");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("NoseConeCfg.lbl.WholeCpCalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final WholeCpRequest request = new net.sf.openrocket.utils.educoder.WholeCpRequest();
                List<WholeCpDTO> wholeCpDTOList = new ArrayList<>();

                //get configuration
                FlightConfiguration curConfig = document.getSelectedConfiguration();
                FlightConditions conditions = new FlightConditions(curConfig);

                //				get calcMap  可以计算cp的组件
                HashMap<RocketComponent, RocketComponentCalc> calcMap = null;
                BarrowmanCalculator aerodynamicCalculator = new BarrowmanCalculator();

                try {
                    Method method = BarrowmanCalculator.class.getDeclaredMethod("buildCalcMap", FlightConfiguration.class);
                    method.setAccessible(true);
                    method.invoke(aerodynamicCalculator, curConfig);
                    Field field = BarrowmanCalculator.class.getDeclaredField("calcMap");
                    field.setAccessible(true);
                    HashMap<RocketComponent, RocketComponentCalc> mapfield = (HashMap<RocketComponent, RocketComponentCalc>) field.get(aerodynamicCalculator);
                    calcMap = mapfield;

                    //get instance
                    //包含所有实例的组件
                    InstanceMap imap = curConfig.getActiveInstances();
                    int i = 1;
                    Double[] machs = arange(0, 1, 0.01);
                    Double[] aoa = arange(-90, 90, 1);
                    FlightConditions conditions_mach = conditions.clone();
                    FlightConditions conditions_aoa = conditions.clone();
                    Double initMach = conditions.getMach();
                    //获取有cp的组件
                    for (Map.Entry<RocketComponent, ArrayList<InstanceContext>> mapEntry : imap.entrySet()) {
                        Coordinate[] cps = new Coordinate[machs.length];
                        Coordinate[] cps2 = new Coordinate[aoa.length];
                        net.sf.openrocket.utils.educoder.WholeCpDTO wholeCpDTO = new net.sf.openrocket.utils.educoder.WholeCpDTO();
                        net.sf.openrocket.utils.educoder.WholeCpDTO wholeCpDTO2 = new net.sf.openrocket.utils.educoder.WholeCpDTO();
                        //循环传入mach
                        for (int j = 0; j < machs.length; j++) {
                            final RocketComponent comp = mapEntry.getKey();
                            final List<InstanceContext> contextList = mapEntry.getValue();
                            conditions_mach.setMach(machs[j]);

                            RocketComponentCalc calcObj = calcMap.get(comp);
                            AerodynamicForces componentForces = null;
                            if (null != calcObj) {
                                Method method2 = BarrowmanCalculator.class.getDeclaredMethod("calculateComponentNonAxialForces",
                                        FlightConditions.class, RocketComponent.class, RocketComponentCalc.class, List.class, WarningSet.class);
                                method2.setAccessible(true);
                                componentForces = (AerodynamicForces) method2.invoke(aerodynamicCalculator, conditions_mach, comp, calcObj, contextList, new WarningSet());
                                Coordinate np = new Coordinate(componentForces.getCP().x, componentForces.getCP().y, componentForces.getCP().z);
                                cps[j] = componentForces.getCP();
                                //set ui
                                if (initMach == conditions_mach.getMach()) {
                                    String labelText = comp.getComponentName() + " 组件压心: " + np;
                                    String labelText2 = comp.getComponentName() + "组件位置: " + comp.getPosition();
                                    String constraints = "newline, height 30!";
                                    String constraints2 = "height 30!";
                                    if (i < 11) {
                                        dialog.add(new JLabel(labelText), constraints);
                                        dialog.add(new JLabel(labelText2), constraints2);
                                    }
                                    i++;

                                }

                            }
                        }
                        //set request
                        wholeCpDTO.setComponentName(mapEntry.getKey().getComponentName());
                        wholeCpDTO.setCp(cps);
                        wholeCpDTO.setRocketComponentCalc(calcMap.get(mapEntry.getKey()) == null ? Boolean.FALSE : Boolean.TRUE);
                        wholeCpDTOList.add(wholeCpDTO);
                        // 循环传入aoa
                        for (int j = 0; j < aoa.length; j++) {
                            final RocketComponent comp = mapEntry.getKey();
                            final List<InstanceContext> contextList = mapEntry.getValue();
                            conditions_aoa.setAOA(aoa[j] * (Math.PI / 180));

                            RocketComponentCalc calcObj = calcMap.get(comp);
                            AerodynamicForces componentForces = null;
                            if (null != calcObj) {
                                //reflect
                                Method method2 = BarrowmanCalculator.class.getDeclaredMethod("calculateComponentNonAxialForces",
                                        FlightConditions.class, RocketComponent.class, RocketComponentCalc.class, List.class, WarningSet.class);
                                method2.setAccessible(true);
                                componentForces = (AerodynamicForces) method2.invoke(aerodynamicCalculator, conditions_aoa, comp, calcObj, contextList, new WarningSet());
                                cps2[j] = componentForces.getCP();

                            }
                        }
                        //set request
                        wholeCpDTO2.setComponentName(mapEntry.getKey().getComponentName());
                        wholeCpDTO2.setCp(cps2);
                        wholeCpDTO2.setRocketComponentCalc(calcMap.get(mapEntry.getKey()) == null ? Boolean.FALSE : Boolean.TRUE);
                        wholeCpDTOList.add(wholeCpDTO2);

                    }


                    request.setList(wholeCpDTOList);
                    Coordinate cp = aerodynamicCalculator.getCP(document.getSelectedConfiguration(),
                            new FlightConditions(document.getSelectedConfiguration()), new WarningSet());
                    request.setAnswer(cp.x);

                    JButton checkButton = new JButton(trans.get("NoseConeCfg.lbl.check"));
                    JLabel checkResult = new JLabel(trans.get("NoseConeCfg.lbl.checkResult") + ": ");
                    JLabel answerLabel = new JLabel(trans.get("NoseConeCfg.lbl.answer") + ": ");
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
                                    checkResult.setText(trans.get("NoseConeCfg.lbl.checkResult") + ": " + result.getResult());
                                    String msg = "";
                                    double answer =  cp.x;
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
                    //ignore
                    ex.printStackTrace();
                }
                dialog.setVisible(true);

                //map---->wholeCPDTO
            });
        }

        { // MOI calculate:
            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.MOICalc") + ":"));
            JButton button = new JButton(trans.get("NoseConeCfg.lbl.MOIEnter"));
            panel.add(button);
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("NoseConeCfg.lbl.MOICalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.NoseConeMOIRequest request = new net.sf.openrocket.utils.educoder.NoseConeMOIRequest();
                // answer = rotationalUnitInertia
                request.setAnswer(new Double[]{component.getRotationalUnitInertia(), component.getLongitudinalUnitInertia()});
                String[] transitionMethodNames = {"getForeRadius", "getAftRadius"};
                String[] transitionFieldNames = {"shapeParameter", "type"};
                String[] fieldNames = {"filled", "thickness", "DIVISIONS"};

                try {
                    //get and set  properties
                    for (String fieldName : fieldNames) {
                        Field field = SymmetricComponent.class.getDeclaredField(fieldName);
                        Field reqField = net.sf.openrocket.utils.educoder.NoseConeMOIRequest.class.getDeclaredField(fieldName);
                        field.setAccessible(true);
                        reqField.setAccessible(true);
                        Object value = field.get(component);
                        reqField.set(request, value);
                        String labelText = trans.get("NoseConeCfg.lbl." + fieldName) + ": " + value;
                        String constraints = (fieldName.equals(fieldNames[0])) ? "spanx, height 30!" : "newline, height 30!";
                        dialog.add(new JLabel(labelText), constraints);
                    }

                    //set len
                    double length = component.getLength();
                    request.setLength(length);
                    request.setDensity(((NoseCone) component).getMaterial().getDensity());
                    String lengthLabelText = trans.get("NoseConeCfg.lbl.length") + ": " + length;
                    dialog.add(new JLabel(lengthLabelText), "newline, height 30!");

                    //set transitionMethodNames
                    for (String fieldName : transitionFieldNames) {
                        Field field = Transition.class.getDeclaredField(fieldName);
                        String reqFieldName = "transition" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                        Field reqField = net.sf.openrocket.utils.educoder.NoseConeMOIRequest.class.getDeclaredField(reqFieldName);
                        field.setAccessible(true);
                        reqField.setAccessible(true);
                        Object value = field.get(component);
                        reqField.set(request, value);
                        String labelText = trans.get("NoseConeCfg.lbl." + reqFieldName) + ": " + value;
                        if (value instanceof Transition.Shape)
                            labelText = trans.get("NoseConeCfg.lbl." + reqFieldName) + ": " + ((Transition.Shape) value).name();
                        dialog.add(new JLabel(labelText), "newline, height 30!");
                    }
                    //set transitionMethodNames
                    request.setForeShoulderLength(((NoseCone) component).getForeShoulderLength());
                    request.setForeShoulderRadius(((NoseCone) component).getForeShoulderRadius());
                    request.setAftShoulderRadius(((NoseCone) component).getAftShoulderRadius());
                    request.setForeShoulderThickness(((NoseCone) component).getForeShoulderThickness());
                    request.setAftShoulderLength(((NoseCone) component).getAftShoulderLength());
                    request.setAftShoulderThickness(((NoseCone) component).getAftShoulderThickness());
                    request.setForeShoulderCapped(((NoseCone) component).isForeShoulderCapped());
                    request.setAftShoulderCapped(((NoseCone) component).isAftShoulderCapped());
                    // AftRadius
                    for (String methodName : transitionMethodNames) {
                        Method method = Transition.class.getDeclaredMethod(methodName);
                        Method reqMethod = net.sf.openrocket.utils.educoder.NoseConeMOIRequest.class
                                .getDeclaredMethod(methodName.replaceFirst("get", "setTransition"), Double.class);
                        Double value = (Double) method.invoke(component); // All values are double type
                        reqMethod.invoke(request, value);
                        String labelText = trans.get("NoseConeCfg.lbl." + methodName.replaceFirst("get", "transition")) + ": " + value;
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
                                    double answer = component.getRotationalUnitInertia();
                                    double answer2 = component.getLongitudinalInertia();
                                    double resultResult = (double) result.getResult()[0];
                                    double resultResult2 = (double) result.getResult()[1];
                                    if (roundToFiveDecimals(answer)==roundToFiveDecimals(resultResult)){
                                        msg = "答案:"+String.valueOf(answer)+","+answer2;
                                    }else {
                                        double absError = Math.abs(resultResult - answer) * 0.1;
                                        double relativeError = (answer != 0) ? (absError / Math.abs(answer)) * 100 : 0;
                                        msg = "误差："+String.valueOf(relativeError)+"%";

                                        double absError2 = Math.abs(resultResult2 - answer2) * 0.1;
                                        double relativeError2 = (answer2 != 0) ? (absError2 / Math.abs(answer2)) * 100 : 0;
                                        msg2 = "误差："+String.valueOf(relativeError2)+"%";
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

        { // Whole MOI calculate:
            panel.add(new JLabel(trans.get("NoseConeCfg.lbl.WholeMOICalc") + ":"), "gapleft 15px");
            JButton button = new JButton(trans.get("NoseConeCfg.lbl.MOIEnter"));
            panel.add(button, "span");
            button.addActionListener(e -> {
                JDialog dialog = new JDialog(this.parent, trans.get("NoseConeCfg.lbl.WholeMOICalc"));
                dialog.setSize(this.parent.getSize());
                dialog.setLocationRelativeTo(null);
                dialog.setLayout(new MigLayout("fill, gap 4!, ins panel, hidemode 3", "[]:5[]", "[]:5[]"));

                final net.sf.openrocket.utils.educoder.WholeMOIRequest request = new net.sf.openrocket.utils.educoder.WholeMOIRequest();


                try {
                    FlightConfiguration configuration = document.getSelectedConfiguration();

                    RocketComponent rocket = document.getSelectedConfiguration().getRocket();
                    net.sf.openrocket.utils.educoder.WholeMOIDTO rootComponent = new net.sf.openrocket.utils.educoder.WholeMOIDTO();
                    //copy field
                    rootComponent.copyValues(rocket, document.getSelectedConfiguration());

                    request.setWholeMOIDTO(rootComponent);

                    MassCalculation calculation = new MassCalculation(MassCalculation.Type.LAUNCH, configuration,
                            Motor.PSEUDO_TIME_LAUNCH, null, configuration.getRocket(), Transformation.IDENTITY, null);
                    Method method = MassCalculation.class.getDeclaredMethod("calculateAssembly");
                    method.setAccessible(true);
                    method.invoke(calculation);
                    RigidBody rigidBody = MassCalculator.calculateLaunch(configuration);
                    request.setAnswer(new Double[]{rigidBody.Ixx, rigidBody.Iyy});

                    int i = 1;
                    for (RocketComponent component1 : rocket.getAllChildren()) {
                        if (component1.getComponentName().equals("火箭") || component1.getComponentName().equals("火箭级"))
                            continue;
                        String labelText = component1.getComponentName() + " 组件重心: " + component1.getComponentCG().x;
                        String labelText2 = component1.getComponentName() + "横向转动惯量: " + component1.getRotationalUnitInertia();
                        String labelText3 = component1.getComponentName() + "纵向转动惯量: " + component1.getLongitudinalUnitInertia();
                        String constraints = "newline, height 30!";
                        String constraints2 = "height 30!";
                        if (i < 11) {
                            dialog.add(new JLabel(labelText), constraints);
                            dialog.add(new JLabel(labelText2), constraints2);
                            dialog.add(new JLabel(labelText3), constraints2);
                        }
                        i++;

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
                                    double answer =  rigidBody.Ixx;
                                    double answer2 = rigidBody.Iyy;
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
                    ex.printStackTrace();
                }
                dialog.setVisible(true);
            });
        }
        {//// Flip to tail cone:
            BooleanModel bm = new BooleanModel(component, "Flipped");
            register(bm);
            final JCheckBox flipCheckbox = new JCheckBox(bm);
            flipCheckbox.setText(trans.get("NoseConeCfg.checkbox.Flip"));
            flipCheckbox.setToolTipText(trans.get("NoseConeCfg.checkbox.Flip.ttip"));
            panel.add(flipCheckbox, "spanx, wrap");
            order.add(flipCheckbox);
            flipCheckbox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    updateCheckboxAutoBaseRadius(e.getStateChange() == ItemEvent.SELECTED);
                }
            });
        }


        panel.add(new JLabel(""), "growy");

        ////  Description

        JPanel panel2 = new JPanel(new MigLayout("ins 0"));

        description = new DescriptionArea(5);
        description.setText(PREDESC + ((NoseCone) component).getShapeType().getNoseConeDescription());
        panel2.add(description, "wmin 250lp, spanx, growx, wrap para");


        //// Material
        MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
        register(materialPanel);
        panel2.add(materialPanel, "span, wrap");
        panel.add(panel2, "cell 4 0, gapleft 40lp, aligny 0%, spany");


        //// General and General properties
        tabbedPane.insertTab(trans.get("NoseConeCfg.tab.General"), null, panel,
                trans.get("NoseConeCfg.tab.ttip.General"), 0);
        //// Shoulder and Shoulder properties
        tabbedPane.insertTab(trans.get("NoseConeCfg.tab.Shoulder"), null, shoulderTab(),
                trans.get("NoseConeCfg.tab.ttip.Shoulder"), 1);
        tabbedPane.setSelectedIndex(0);

        // Apply the custom focus travel policy to this config dialog
        //// Make sure the cancel & ok button is the last component
        order.add(cancelButton);
        order.add(okButton);
        CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
        parent.setFocusTraversalPolicy(policy);
    }


    private void updateEnabled() {
        boolean e = ((NoseCone) component).getShapeType().usesParameter();
        shapeLabel.setEnabled(e);
        shapeSpinner.setEnabled(e);
        shapeSlider.setEnabled(e);
    }

    /**
     * Sets the checkAutoAftRadius checkbox's enabled state and tooltip text, based on the state of its next component.
     * If there is no next symmetric component or if that component already has its auto checkbox checked, the
     * checkAutoAftRadius checkbox is disabled.
     *
     * @param isFlipped whether the nose cone is flipped
     */
    private void updateCheckboxAutoBaseRadius(boolean isFlipped) {
        if (component == null || checkAutoBaseRadius == null) return;

        // Disable check button if there is no component to get the diameter from
        NoseCone noseCone = ((NoseCone) component);
        SymmetricComponent referenceComp = isFlipped ? noseCone.getPreviousSymmetricComponent() : noseCone.getNextSymmetricComponent();
        if (referenceComp == null) {
            checkAutoBaseRadius.setEnabled(false);
            ((NoseCone) component).setBaseRadiusAutomatic(false);
            checkAutoBaseRadius.setToolTipText(trans.get("NoseConeCfg.checkbox.ttip.Automatic_noReferenceComponent"));
            return;
        }
        if ((!isFlipped && !referenceComp.usesPreviousCompAutomatic()) ||
                isFlipped && !referenceComp.usesNextCompAutomatic()) {
            checkAutoBaseRadius.setEnabled(true);
            checkAutoBaseRadius.setToolTipText(trans.get("NoseConeCfg.checkbox.ttip.Automatic"));
        } else {
            checkAutoBaseRadius.setEnabled(false);
            ((NoseCone) component).setBaseRadiusAutomatic(false);
            checkAutoBaseRadius.setToolTipText(trans.get("NoseConeCfg.checkbox.ttip.Automatic_alreadyAuto"));
        }
    }

    public static Double[] arange(double start, double end, double step) {
        // 计算数组的大小
        int size = (int) ((end - start) / step);

        // 创建数组
        Double[] rangeArray = new Double[size];

        // 填充数组
        for (int i = 0; i < size; i++) {
            rangeArray[i] = start + i * step;
        }

        return rangeArray;
    }
    public static double roundToFiveDecimals(double value) {
        return Math.round(value * 100000.0) / 100000.0;
    }


}
