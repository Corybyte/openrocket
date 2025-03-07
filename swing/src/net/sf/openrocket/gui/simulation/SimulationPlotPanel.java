package net.sf.openrocket.gui.simulation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serial;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.xml.crypto.Data;

import com.oracle.truffle.js.nodes.access.LocalVarIncNode;
import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.BarrowmanCalculator;
import net.sf.openrocket.aerodynamics.FlightConditions;
import net.sf.openrocket.aerodynamics.barrowman.RocketComponentCalc;
import net.sf.openrocket.aerodynamics.barrowman.SymmetricComponentCalc;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.OpenRocketDocumentFactory;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.plot.PlotConfiguration;
import net.sf.openrocket.gui.plot.SimulationPlotDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.gui.util.UITheme;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.WarningSet;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.simulation.FlightDataBranch;
import net.sf.openrocket.simulation.FlightDataType;
import net.sf.openrocket.simulation.FlightDataTypeGroup;
import net.sf.openrocket.simulation.FlightEvent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.OpenRocket;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Utils;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.utils.educoder.*;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static net.sf.openrocket.gui.simulation.SimulationExportPanel.removeTimestamp;
import static net.sf.openrocket.gui.simulation.SimulationExportPanel.sortByTimestamp;

/**
 * Panel that displays the simulation plot options to the user.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimulationPlotPanel extends JPanel {
    @Serial
    private static final long serialVersionUID = -2227129713185477998L;

    private static final Translator trans = Application.getTranslator();
    private static final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();

    // TODO: LOW: Should these be somewhere else?
    public static final int AUTO = -1;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    //// Auto
    public static final String AUTO_NAME = trans.get("simplotpanel.AUTO_NAME");
    //// Left
    public static final String LEFT_NAME = trans.get("simplotpanel.LEFT_NAME");
    //// Right
    public static final String RIGHT_NAME = trans.get("simplotpanel.RIGHT_NAME");

    //// Custom
    private static final String CUSTOM = trans.get("simplotpanel.CUSTOM");

    /**
     * The "Custom" configuration - not to be used for anything other than the title.
     */
    private static final PlotConfiguration CUSTOM_CONFIGURATION;

    static {
        CUSTOM_CONFIGURATION = new PlotConfiguration(CUSTOM);
    }

    /**
     * The array of presets for the combo box.
     */
    private static final PlotConfiguration[] PRESET_ARRAY;

    static {
        PRESET_ARRAY = Arrays.copyOf(PlotConfiguration.DEFAULT_CONFIGURATIONS,
                PlotConfiguration.DEFAULT_CONFIGURATIONS.length + 1);
        PRESET_ARRAY[PRESET_ARRAY.length - 1] = CUSTOM_CONFIGURATION;
    }


    /**
     * The current default configuration, set each time a plot is made.
     */
    private static PlotConfiguration defaultConfiguration =
            PlotConfiguration.DEFAULT_CONFIGURATIONS[0].resetUnits();


    private final Simulation simulation;
    private final FlightDataType[] types;
    private PlotConfiguration configuration;


    private JComboBox<PlotConfiguration> configurationSelector;

    private JComboBox<FlightDataType> domainTypeSelector;
    private UnitSelector domainUnitSelector;

    private JPanel typeSelectorPanel;
    private FlightEventTableModel eventTableModel;


    private int modifying = 0;

    private DescriptionArea simPlotPanelDesc;

    private static java.awt.Color darkErrorColor;
    private static Border border;

    static {
        initColors();
    }

    public SimulationPlotPanel(final Simulation simulation) {
        super(new MigLayout("fill"));

        this.simulation = simulation;
        if (simulation.getSimulatedData() == null ||
                simulation.getSimulatedData().getBranchCount() == 0) {
            throw new IllegalArgumentException("Simulation contains no data.");
        }
        FlightDataBranch branch = simulation.getSimulatedData().getBranch(0);
        types = branch.getTypes();

        setConfiguration(defaultConfiguration);

        ////  Configuration selector

        // Setup the combo box
        configurationSelector = new JComboBox<PlotConfiguration>(PRESET_ARRAY);
        for (PlotConfiguration config : PRESET_ARRAY) {
            if (config.getName().equals(configuration.getName())) {
                configurationSelector.setSelectedItem(config);
            }
        }

        configurationSelector.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                // We are only concerned with ItemEvent.SELECTED to update
                // the UI when the selected item changes.
                // TODO - this should probably be implemented as an ActionListener instead
                // of ItemStateListener.
                if (e.getStateChange() == ItemEvent.DESELECTED) {
                    return;
                }
                if (modifying > 0)
                    return;
                PlotConfiguration conf = (PlotConfiguration) configurationSelector.getSelectedItem();
                if (conf == CUSTOM_CONFIGURATION)
                    return;
                modifying++;
                setConfiguration(conf.clone().resetUnits());
                updatePlots();
                modifying--;
            }
        });
        //// Preset plot configurations:
        this.add(new JLabel(trans.get("simplotpanel.lbl.Presetplotconf")), "spanx, split");
        this.add(configurationSelector, "growx, wrap 20lp");


        //// X axis

        //// X axis type:
        this.add(new JLabel(trans.get("simplotpanel.lbl.Xaxistype")), "spanx, split");
        domainTypeSelector = FlightDataComboBox.createComboBox(FlightDataTypeGroup.ALL_GROUPS, types);
        domainTypeSelector.setSelectedItem(configuration.getDomainAxisType());
        domainTypeSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (modifying > 0)
                    return;
                FlightDataType type = (FlightDataType) domainTypeSelector.getSelectedItem();
                if (type == FlightDataType.TYPE_TIME) {
                    simPlotPanelDesc.setVisible(false);
                    simPlotPanelDesc.setText("");
                } else {
                    simPlotPanelDesc.setVisible(true);
                    simPlotPanelDesc.setText(trans.get("simplotpanel.Desc"));
                }
                configuration.setDomainAxisType(type);
                domainUnitSelector.setUnitGroup(type.getUnitGroup());
                domainUnitSelector.setSelectedUnit(configuration.getDomainAxisUnit());
                setToCustom();
            }
        });
        this.add(domainTypeSelector, "gapright para");

        //// Unit:
        this.add(new JLabel(trans.get("simplotpanel.lbl.Unit")));
        domainUnitSelector = new UnitSelector(configuration.getDomainAxisType().getUnitGroup());
        domainUnitSelector.setSelectedUnit(configuration.getDomainAxisUnit());
        domainUnitSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (modifying > 0)
                    return;
                configuration.setDomainAxisUnit(domainUnitSelector.getSelectedUnit());
            }
        });
        this.add(domainUnitSelector, "width 40lp, gapright para");

        //// The data will be plotted in time order even if the X axis type is not time.
        simPlotPanelDesc = new DescriptionArea("", 2, -2f, false);
        simPlotPanelDesc.setVisible(false);
        simPlotPanelDesc.setForeground(darkErrorColor);
        simPlotPanelDesc.setViewportBorder(BorderFactory.createEmptyBorder());
        this.add(simPlotPanelDesc, "width 1px, growx 1, wrap unrel");


        //// Y axis selector panel
        //// Y axis types:
        this.add(new JLabel(trans.get("simplotpanel.lbl.Yaxistypes")));
        //// Flight events:
        this.add(new JLabel(trans.get("simplotpanel.lbl.Flightevents")), "wrap rel");

        typeSelectorPanel = new JPanel(new MigLayout("gapy rel"));
        JScrollPane scroll = new JScrollPane(typeSelectorPanel);
        scroll.setBorder(border);
        this.add(scroll, "spany 3, height 10px, wmin 400lp, grow 100, gapright para");


        //// Flight events
        eventTableModel = new FlightEventTableModel();
        JTable table = new JTable(eventTableModel);
        table.setTableHeader(null);
        table.setShowVerticalLines(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);

        TableColumnModel columnModel = table.getColumnModel();
        TableColumn col0 = columnModel.getColumn(0);
        int w = table.getRowHeight() + 2;
        col0.setMinWidth(w);
        col0.setPreferredWidth(w);
        col0.setMaxWidth(w);
        table.addMouseListener(new GUIUtil.BooleanTableClickListener(table));
        this.add(new JScrollPane(table), "height 200px, width 200lp, grow 1, wrap rel");


        ////  All + None buttons
        JButton button = new SelectColorButton(trans.get("simplotpanel.but.All"));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (FlightEvent.Type t : FlightEvent.Type.values())
                    configuration.setEvent(t, true);
                eventTableModel.fireTableDataChanged();
            }
        });
        this.add(button, "split 2, gapleft para, gapright para, growx, sizegroup buttons");

        //// None
        button = new SelectColorButton(trans.get("simplotpanel.but.None"));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (FlightEvent.Type t : FlightEvent.Type.values())
                    configuration.setEvent(t, false);
                eventTableModel.fireTableDataChanged();
            }
        });
        this.add(button, "gapleft para, gapright para, growx, sizegroup buttons, wrap");


        //// Style event marker
        JLabel styleEventMarker = new JLabel(trans.get("simplotpanel.MarkerStyle.lbl.MarkerStyle"));
        JRadioButton radioVerticalMarker = new JRadioButton(trans.get("simplotpanel.MarkerStyle.btn.VerticalMarker"));
        JRadioButton radioIcon = new JRadioButton(trans.get("simplotpanel.MarkerStyle.btn.Icon"));
        ButtonGroup bg = new ButtonGroup();
        bg.add(radioVerticalMarker);
        bg.add(radioIcon);

        boolean useIcon = preferences.getBoolean(Preferences.MARKER_STYLE_ICON, false);
        if (useIcon) {
            radioIcon.setSelected(true);
        } else {
            radioVerticalMarker.setSelected(true);
        }

        radioIcon.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (modifying > 0)
                    return;
                preferences.putBoolean(Preferences.MARKER_STYLE_ICON, radioIcon.isSelected());
            }
        });

        domainTypeSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateStyleEventWidgets(styleEventMarker, radioVerticalMarker, radioIcon);
            }
        });
        updateStyleEventWidgets(styleEventMarker, radioVerticalMarker, radioIcon);

        this.add(styleEventMarker, "split 3, growx");
        this.add(radioVerticalMarker);
        this.add(radioIcon, "wrap para");


        //// New Y axis plot type
        button = new SelectColorButton(trans.get("simplotpanel.but.NewYaxisplottype"));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (configuration.getTypeCount() >= 15) {
                    JOptionPane.showMessageDialog(SimulationPlotPanel.this,
                            //// A maximum of 15 plots is allowed.
                            //// Cannot add plot
                            trans.get("simplotpanel.OptionPane.lbl1"),
                            trans.get("simplotpanel.OptionPane.lbl2"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Select new type smartly
                FlightDataType type = null;
                for (FlightDataType t :
                        simulation.getSimulatedData().getBranch(0).getTypes()) {

                    boolean used = false;
                    if (configuration.getDomainAxisType().equals(t)) {
                        used = true;
                    } else {
                        for (int i = 0; i < configuration.getTypeCount(); i++) {
                            if (configuration.getType(i).equals(t)) {
                                used = true;
                                break;
                            }
                        }
                    }

                    if (!used) {
                        type = t;
                        break;
                    }
                }
                if (type == null) {
                    type = simulation.getSimulatedData().getBranch(0).getTypes()[0];
                }

                // Add new type
                configuration.addPlotDataType(type);
                setToCustom();
                updatePlots();
            }
        });
        this.add(button, "spanx, split");


        this.add(new JPanel(), "growx");

		/*
		//// Plot flight
		button = new SelectColorButton(trans.get("simplotpanel.but.Plotflight"));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (configuration.getTypeCount() == 0) {
					JOptionPane.showMessageDialog(SimulationPlotPanel.this,
							trans.get("error.noPlotSelected"),
							trans.get("error.noPlotSelected.title"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				defaultConfiguration = configuration.clone();
				SimulationPlotDialog.showPlot(SwingUtilities.getWindowAncestor(SimulationPlotPanel.this),
						simulation, configuration);
			}
		});
		this.add(button, "right");
		*/
        updatePlots();
    }

    private static void initColors() {
        updateColors();
        UITheme.Theme.addUIThemeChangeListener(SimulationPlotPanel::updateColors);
    }

    private static void updateColors() {
        darkErrorColor = GUIUtil.getUITheme().getDarkErrorColor();
        border = GUIUtil.getUITheme().getBorder();
    }

    private void updateStyleEventWidgets(JLabel styleEventMarker, JRadioButton radioVerticalMarker, JRadioButton radioIcon) {
        if (modifying > 0)
            return;
        FlightDataType type = (FlightDataType) domainTypeSelector.getSelectedItem();
        boolean isTime = type == FlightDataType.TYPE_TIME;
        styleEventMarker.setEnabled(isTime);
        radioVerticalMarker.setEnabled(isTime);
        radioIcon.setEnabled(isTime);
        styleEventMarker.setToolTipText(isTime ? trans.get("simplotpanel.MarkerStyle.lbl.MarkerStyle.ttip") : trans.get("simplotpanel.MarkerStyle.OnlyInTime"));
        radioVerticalMarker.setToolTipText(isTime ? null : trans.get("simplotpanel.MarkerStyle.OnlyInTime"));
        radioIcon.setToolTipText(isTime ? null : trans.get("simplotpanel.MarkerStyle.OnlyInTime"));
    }

    public JDialog doPlot(Window parent) {
        if (configuration.getTypeCount() == 0) {
            JOptionPane.showMessageDialog(SimulationPlotPanel.this,
                    trans.get("error.noPlotSelected"),
                    trans.get("error.noPlotSelected.title"),
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
        defaultConfiguration = configuration.clone();
        return SimulationPlotDialog.getPlot(parent, simulation, configuration);
    }

    private void setConfiguration(PlotConfiguration conf) {

        boolean modified = false;

        configuration = conf.clone();
        if (!Utils.contains(types, configuration.getDomainAxisType())) {
            configuration.setDomainAxisType(types[0]);
            modified = true;
        }

        for (int i = 0; i < configuration.getTypeCount(); i++) {
            if (!Utils.contains(types, configuration.getType(i))) {
                configuration.removePlotDataType(i);
                i--;
                modified = true;
            }
        }

        if (modified) {
            configuration.setName(CUSTOM);
        }

    }


    private void setToCustom() {
        modifying++;
        configuration.setName(CUSTOM);
        configurationSelector.setSelectedItem(CUSTOM_CONFIGURATION);
        modifying--;
    }


    private void updatePlots() {
        domainTypeSelector.setSelectedItem(configuration.getDomainAxisType());
        domainUnitSelector.setUnitGroup(configuration.getDomainAxisType().getUnitGroup());
        domainUnitSelector.setSelectedUnit(configuration.getDomainAxisUnit());

        typeSelectorPanel.removeAll();
        for (int i = 0; i < configuration.getTypeCount(); i++) {
            FlightDataType type = configuration.getType(i);
            Unit unit = configuration.getUnit(i);
            int axis = configuration.getAxis(i);

            typeSelectorPanel.add(new PlotTypeSelector(i, type, unit, axis), "wrap");
        }
        //总体基底阻力ui
        JButton jButton = new SelectColorButton("总体基底阻力计算");
        typeSelectorPanel.add(jButton, "growx 1, sizegroup selectbutton, wrap,newline");

        jButton.addActionListener(e -> {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "总体基底阻力计算", Dialog.ModalityType.MODELESS);
            dialog.setLayout(new BorderLayout());
            // 创建主内容面板，使用 GridLayout 管理两部分内容
            JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 行 2 列，水平间距 10

            // 左边的大文本框
            JTextArea leftTextArea = new JTextArea();
            leftTextArea.setLineWrap(true); // 自动换行
            leftTextArea.setWrapStyleWord(true); // 仅在单词边界处换行
            leftTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 设置字体
            leftTextArea.setText(TotalBasalResistanceRequest.server_cn.toString());
            JScrollPane leftScrollPane = new JScrollPane(leftTextArea);
            mainPanel.add(leftScrollPane);
            sortByTimestamp(TotalPressureCDRequest.client_cn);
            // 右边的小文本框
            JTextArea rightTextArea = new JTextArea();
            rightTextArea.setLineWrap(true); // 自动换行
            rightTextArea.setWrapStyleWord(true); // 仅在单词边界处换行
            rightTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 设置字体
            rightTextArea.setText(TotalBasalResistanceRequest.client_cn.toString());
            JScrollPane rightScrollPane = new JScrollPane(rightTextArea);
            mainPanel.add(rightScrollPane);

            // 将主面板添加到对话框的中间区域
            dialog.add(mainPanel, BorderLayout.CENTER);

            // 创建关闭按钮
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(ev -> dialog.dispose()); // 点击按钮时关闭对话框

            // 创建一个新的按钮
            JButton checkButton = new JButton("数据同步与测评");

            // 创建按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // 水平间距 10
            buttonPanel.add(closeButton); // 添加关闭按钮
            buttonPanel.add(checkButton);   // 添加新按钮
            dialog.add(buttonPanel, BorderLayout.SOUTH); // 将按钮面板放置在底部

            // 设置对话框属性
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // 关闭时释放对话框资源
            dialog.setSize(800, 400); // 设置窗口宽度更大，适应两个文本框
            dialog.setLocationRelativeTo(this); // 设置相对于父窗口居中显示
            dialog.setVisible(true); // 显示对话框
            ArrayList client_cn = removeTimestamp(TotalBasalResistanceRequest.client_cn);
            ArrayList server_cn = removeTimestamp(TotalBasalResistanceRequest.server_cn);

            DataRequest request = new DataRequest(client_cn, server_cn);
            checkButton.addActionListener(e1 -> OpenRocket.eduCoderService.checkJSON(request).enqueue(new Callback<Result>() {
                @Override
                public void onResponse(Call<Result> call, Response<Result> response) {
                    JOptionPane.showMessageDialog(dialog, "请点击平台评测按钮");
                }

                @Override
                public void onFailure(Call<Result> call, Throwable throwable) {
                    System.out.println(throwable.getMessage());
                }
            }));
            if (!client_cn.isEmpty()&&client_cn.get(0) instanceof Double) {
                checkButton.addActionListener(e1 -> {
                    OpenRocket.eduCoderService.calculateCDPLT(client_cn).enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {

                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable throwable) {

                        }
                    });

                });

            }
        });


        //
        //对称组件压差阻力ui
        JButton jButton2 = new SelectColorButton("对称组件压差阻力测评");
        typeSelectorPanel.add(jButton2, "growx 1, sizegroup selectbutton, wrap,newline");

        jButton2.addActionListener(e -> {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "对称组件压差阻力测评", Dialog.ModalityType.MODELESS);
            dialog.setLayout(new BorderLayout());
            // 创建主内容面板，使用 GridLayout 管理两部分内容
            JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 行 2 列，水平间距 10

            // 左边的大文本框
            JTextArea leftTextArea = new JTextArea();
            leftTextArea.setLineWrap(true); // 自动换行
            leftTextArea.setWrapStyleWord(true); // 仅在单词边界处换行
            leftTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 设置字体
            leftTextArea.setText(BodyPressureCDRequest.server_cn.toString());
            JScrollPane leftScrollPane = new JScrollPane(leftTextArea);
            mainPanel.add(leftScrollPane);
            sortByTimestamp(BodyPressureCDRequest.client_cn);
            // 右边的小文本框
            JTextArea rightTextArea = new JTextArea();
            rightTextArea.setLineWrap(true); // 自动换行
            rightTextArea.setWrapStyleWord(true); // 仅在单词边界处换行
            rightTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 设置字体
            rightTextArea.setText(BodyPressureCDRequest.client_cn.toString());
            JScrollPane rightScrollPane = new JScrollPane(rightTextArea);
            mainPanel.add(rightScrollPane);

            // 将主面板添加到对话框的中间区域
            dialog.add(mainPanel, BorderLayout.CENTER);

            // 创建关闭按钮
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(ev -> dialog.dispose()); // 点击按钮时关闭对话框

            // 创建一个新的按钮
            JButton checkButton = new JButton("数据同步与测评");

            // 创建按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // 水平间距 10
            buttonPanel.add(closeButton); // 添加关闭按钮
            buttonPanel.add(checkButton);   // 添加新按钮
            dialog.add(buttonPanel, BorderLayout.SOUTH); // 将按钮面板放置在底部

            // 设置对话框属性
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // 关闭时释放对话框资源
            dialog.setSize(800, 400); // 设置窗口宽度更大，适应两个文本框
            dialog.setLocationRelativeTo(this); // 设置相对于父窗口居中显示
            dialog.setVisible(true); // 显示对话框
            ArrayList client_cn = removeTimestamp(BodyPressureCDRequest.client_cn);
            ArrayList server_cn = removeTimestamp(BodyPressureCDRequest.server_cn);
            DataRequest request = new DataRequest(client_cn, server_cn);
            //点击测评更新值
            checkButton.addActionListener(e1 -> {
                OpenRocket.eduCoderService.checkJSON(request).enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        JOptionPane.showMessageDialog(dialog, "请点击平台评测按钮");

                    }

                    @Override
                    public void onFailure(Call<Result> call, Throwable throwable) {

                    }
                });
            });
            if (!client_cn.isEmpty()&&client_cn.get(0) instanceof Double) {
                checkButton.addActionListener(e1 -> {
                    OpenRocket.eduCoderService.calculatePressureCDPLT(client_cn).enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {

                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable throwable) {

                        }
                    });
                });

            }
        });

        //尾翼压差阻力ui
        JButton jButton3 = new SelectColorButton("尾翼压差阻力测评");
        typeSelectorPanel.add(jButton3, "growx 1, sizegroup selectbutton, wrap,newline");

        jButton3.addActionListener(e -> {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "尾翼压差阻力测评", Dialog.ModalityType.MODELESS);
            dialog.setLayout(new BorderLayout());
            // 创建主内容面板，使用 GridLayout 管理两部分内容
            JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 行 2 列，水平间距 10

            // 左边的大文本框
            JTextArea leftTextArea = new JTextArea();
            leftTextArea.setLineWrap(true); // 自动换行
            leftTextArea.setWrapStyleWord(true); // 仅在单词边界处换行
            leftTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 设置字体
            leftTextArea.setText(FinsetPressureCDRequest.server_cn.toString());
            JScrollPane leftScrollPane = new JScrollPane(leftTextArea);
            mainPanel.add(leftScrollPane);
            sortByTimestamp(FinsetPressureCDRequest.client_cn);

            // 右边的小文本框
            JTextArea rightTextArea = new JTextArea();
            rightTextArea.setLineWrap(true); // 自动换行
            rightTextArea.setWrapStyleWord(true); // 仅在单词边界处换行
            rightTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 设置字体
            rightTextArea.setText(FinsetPressureCDRequest.client_cn.toString());
            JScrollPane rightScrollPane = new JScrollPane(rightTextArea);
            mainPanel.add(rightScrollPane);

            // 将主面板添加到对话框的中间区域
            dialog.add(mainPanel, BorderLayout.CENTER);

            // 创建关闭按钮
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(ev -> dialog.dispose()); // 点击按钮时关闭对话框

            // 创建一个新的按钮
            JButton checkButton = new JButton("数据同步与测评");

            // 创建按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // 水平间距 10
            buttonPanel.add(closeButton); // 添加关闭按钮
            buttonPanel.add(checkButton);   // 添加新按钮
            dialog.add(buttonPanel, BorderLayout.SOUTH); // 将按钮面板放置在底部

            // 设置对话框属性
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // 关闭时释放对话框资源
            dialog.setSize(800, 400); // 设置窗口宽度更大，适应两个文本框
            dialog.setLocationRelativeTo(this); // 设置相对于父窗口居中显示
            dialog.setVisible(true); // 显示对话框
            ArrayList client_cn = removeTimestamp(FinsetPressureCDRequest.client_cn);
            ArrayList server_cn = removeTimestamp(FinsetPressureCDRequest.server_cn);
            DataRequest request = new DataRequest(client_cn, server_cn);
            System.out.println(FinsetPressureCDRequest.client_cn);
            System.out.println(FinsetPressureCDRequest.server_cn);
            System.out.println(FinsetPressureCDRequest.server_cn.size());
            System.out.println(FinsetPressureCDRequest.client_cn.size());

            //点击测评更新值
            checkButton.addActionListener(e1 -> {
                OpenRocket.eduCoderService.checkJSON(request).enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        JOptionPane.showMessageDialog(dialog, "请点击平台评测按钮");

                    }

                    @Override
                    public void onFailure(Call<Result> call, Throwable throwable) {

                    }
                });
            });
            if (!client_cn.isEmpty()&&client_cn.get(0) instanceof Double) {
                checkButton.addActionListener(e1 -> {
                    OpenRocket.eduCoderService.calculateFinsetPressureCDPLT(client_cn).enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {

                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable throwable) {

                        }
                    });
                });
            }
        });

        //轴向力系数ui
        JButton jButton4 = new SelectColorButton("轴向力系数测评");
        typeSelectorPanel.add(jButton4, "growx 1, sizegroup selectbutton, wrap,newline");

        jButton4.addActionListener(e -> {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "轴向力系数测评", Dialog.ModalityType.MODELESS);
            dialog.setLayout(new BorderLayout());
            // 创建主内容面板，使用 GridLayout 管理两部分内容
            JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 行 2 列，水平间距 10

            // 左边的大文本框
            JTextArea leftTextArea = new JTextArea();
            leftTextArea.setLineWrap(true); // 自动换行
            leftTextArea.setWrapStyleWord(true); // 仅在单词边界处换行
            leftTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 设置字体
            leftTextArea.setText(AxialCDRequest.server_cn.toString());
            JScrollPane leftScrollPane = new JScrollPane(leftTextArea);
            mainPanel.add(leftScrollPane);

            sortByTimestamp(AxialCDRequest.client_cn);
            // 右边的小文本框
            JTextArea rightTextArea = new JTextArea();
            rightTextArea.setLineWrap(true); // 自动换行
            rightTextArea.setWrapStyleWord(true); // 仅在单词边界处换行
            rightTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 设置字体
            rightTextArea.setText(AxialCDRequest.client_cn.toString());
            JScrollPane rightScrollPane = new JScrollPane(rightTextArea);
            mainPanel.add(rightScrollPane);

            // 将主面板添加到对话框的中间区域
            dialog.add(mainPanel, BorderLayout.CENTER);

            // 创建关闭按钮
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(ev -> dialog.dispose()); // 点击按钮时关闭对话框

            // 创建一个新的按钮
            JButton newButton = new JButton("数据同步与测评");

            // 创建按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // 水平间距 10
            buttonPanel.add(closeButton); // 添加关闭按钮
            buttonPanel.add(newButton);   // 添加新按钮
            dialog.add(buttonPanel, BorderLayout.SOUTH); // 将按钮面板放置在底部

            // 设置对话框属性
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // 关闭时释放对话框资源
            dialog.setSize(800, 400); // 设置窗口宽度更大，适应两个文本框
            dialog.setLocationRelativeTo(this); // 设置相对于父窗口居中显示
            dialog.setVisible(true); // 显示对话框
            ArrayList client_cn = removeTimestamp(AxialCDRequest.client_cn);
            ArrayList server_cn = removeTimestamp(AxialCDRequest.server_cn);
            DataRequest request = new DataRequest(client_cn, server_cn);

            //点击测评更新值
            newButton.addActionListener(e1 -> {
                OpenRocket.eduCoderService.checkJSON(request).enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        JOptionPane.showMessageDialog(dialog, "请点击平台评测按钮");
                    }

                    @Override
                    public void onFailure(Call<Result> call, Throwable throwable) {

                    }
                });
            });
            if (!client_cn.isEmpty()&&client_cn.get(0) instanceof Double) {
                newButton.addActionListener(e1 -> {


                    OpenRocket.eduCoderService.calculateAxialCDPLT(client_cn).enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {

                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable throwable) {

                        }
                    });
                });

            }
        });

        //总体摩擦阻力ui
        JButton jButton5 = new SelectColorButton("总体摩擦阻力测评");
        typeSelectorPanel.add(jButton5, "growx 1, sizegroup selectbutton, wrap,newline");

        jButton5.addActionListener(e -> {
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "总体摩擦阻力测评", Dialog.ModalityType.MODELESS);
            dialog.setLayout(new BorderLayout());
            // 创建主内容面板，使用 GridLayout 管理两部分内容
            JPanel mainPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // 1 行 2 列，水平间距 10

            // 左边的大文本框
            JTextArea leftTextArea = new JTextArea();
            leftTextArea.setLineWrap(true); // 自动换行
            leftTextArea.setWrapStyleWord(true); // 仅在单词边界处换行
            leftTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 设置字体
            leftTextArea.setText(FrictionCDRequest.server_cn.toString());
            JScrollPane leftScrollPane = new JScrollPane(leftTextArea);
            mainPanel.add(leftScrollPane);
            sortByTimestamp(FrictionCDRequest.client_cn);
            // 右边的小文本框
            JTextArea rightTextArea = new JTextArea();
            rightTextArea.setLineWrap(true); // 自动换行
            rightTextArea.setWrapStyleWord(true); // 仅在单词边界处换行
            rightTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14)); // 设置字体
            rightTextArea.setText(FrictionCDRequest.client_cn.toString());
            JScrollPane rightScrollPane = new JScrollPane(rightTextArea);
            mainPanel.add(rightScrollPane);

            // 将主面板添加到对话框的中间区域
            dialog.add(mainPanel, BorderLayout.CENTER);

            // 创建关闭按钮
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(ev -> dialog.dispose()); // 点击按钮时关闭对话框

            // 创建一个新的按钮
            JButton newButton = new JButton("数据同步与测评");

            // 创建按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); // 水平间距 10
            buttonPanel.add(closeButton); // 添加关闭按钮
            buttonPanel.add(newButton);   // 添加新按钮
            dialog.add(buttonPanel, BorderLayout.SOUTH); // 将按钮面板放置在底部

            // 设置对话框属性
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // 关闭时释放对话框资源
            dialog.setSize(800, 400); // 设置窗口宽度更大，适应两个文本框
            dialog.setLocationRelativeTo(this); // 设置相对于父窗口居中显示
            dialog.setVisible(true); // 显示对话框

            net.sf.openrocket.util.ArrayList client_cn = removeTimestamp(FrictionCDRequest.client_cn);
            net.sf.openrocket.util.ArrayList server_cn = removeTimestamp(FrictionCDRequest.server_cn);

            DataRequest request = new DataRequest(client_cn, server_cn);
            //点击测评更新值
            newButton.addActionListener(e1 -> {
                OpenRocket.eduCoderService.checkJSON(request).enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        JOptionPane.showMessageDialog(dialog, "请点击平台评测按钮");


                    }

                    @Override
                    public void onFailure(Call<Result> call, Throwable throwable) {

                    }
                });
            });
            if (! client_cn.isEmpty()&&client_cn.get(0) instanceof Double) {
                newButton.addActionListener(e1 -> {

                    OpenRocket.eduCoderService.calculateFrictionCDPLT(client_cn).enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {

                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable throwable) {

                        }
                    });
                });

            }
        });

        // In order to consistantly update the ui, we need to validate before repaint.
        typeSelectorPanel.validate();
        typeSelectorPanel.repaint();

        eventTableModel.fireTableDataChanged();

    }


    /**
     * A JPanel which configures a single plot of a PlotConfiguration.
     */
    private class PlotTypeSelector extends JPanel {
        private static final long serialVersionUID = 9056324972817542570L;

        private final String[] POSITIONS = {AUTO_NAME, LEFT_NAME, RIGHT_NAME};

        private final int index;
        private final JComboBox<FlightDataType> typeSelector;
        private UnitSelector unitSelector;
        private JComboBox<String> axisSelector;


        public PlotTypeSelector(int plotIndex, FlightDataType type, Unit unit, int position) {
            super(new MigLayout("ins 0"));

            this.index = plotIndex;

            typeSelector = FlightDataComboBox.createComboBox(FlightDataTypeGroup.ALL_GROUPS, types);
            typeSelector.setSelectedItem(type);
            typeSelector.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (modifying > 0)
                        return;
                    FlightDataType selectedType = (FlightDataType) typeSelector.getSelectedItem();
                    configuration.setPlotDataType(index, selectedType);
                    unitSelector.setUnitGroup(selectedType.getUnitGroup());
                    unitSelector.setSelectedUnit(configuration.getUnit(index));
                    setToCustom();
                }
            });
            this.add(typeSelector, "gapright para");

            //// Unit:
            this.add(new JLabel(trans.get("simplotpanel.lbl.Unit")));
            unitSelector = new UnitSelector(type.getUnitGroup());
            if (unit != null)
                unitSelector.setSelectedUnit(unit);
            unitSelector.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (modifying > 0)
                        return;
                    Unit selectedUnit = unitSelector.getSelectedUnit();
                    configuration.setPlotDataUnit(index, selectedUnit);
                }
            });
            this.add(unitSelector, "width 40lp, gapright para");

            //// Axis:
            this.add(new JLabel(trans.get("simplotpanel.lbl.Axis")));
            axisSelector = new JComboBox<String>(POSITIONS);
            if (position == LEFT)
                axisSelector.setSelectedIndex(1);
            else if (position == RIGHT)
                axisSelector.setSelectedIndex(2);
            else
                axisSelector.setSelectedIndex(0);
            axisSelector.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (modifying > 0)
                        return;
                    int axis = axisSelector.getSelectedIndex() - 1;
                    configuration.setPlotDataAxis(index, axis);
                }
            });
            this.add(axisSelector);


            JButton button = new SelectColorButton(Icons.EDIT_DELETE);
            //// Remove this plot
            button.setToolTipText(trans.get("simplotpanel.but.ttip.Deletethisplot"));
            button.setBorderPainted(false);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    configuration.removePlotDataType(index);
                    setToCustom();
                    updatePlots();
                }
            });
            this.add(button, "gapright 0");
        }
    }


    private class FlightEventTableModel extends AbstractTableModel {
        private static final long serialVersionUID = -1108240805614567627L;
        private final FlightEvent.Type[] eventTypes;

        public FlightEventTableModel() {
            EnumSet<FlightEvent.Type> set = EnumSet.noneOf(FlightEvent.Type.class);
            for (int i = 0; i < simulation.getSimulatedData().getBranchCount(); i++) {
                for (FlightEvent e : simulation.getSimulatedData().getBranch(i).getEvents()) {
                    set.add(e.getType());
                }
            }
            set.remove(FlightEvent.Type.ALTITUDE);
            int count = set.size();

            eventTypes = new FlightEvent.Type[count];
            int pos = 0;
            for (FlightEvent.Type t : FlightEvent.Type.values()) {
                if (set.contains(t)) {
                    eventTypes[pos] = t;
                    pos++;
                }
            }
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return eventTypes.length;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return Boolean.class;

                case 1:
                    return String.class;

                default:
                    throw new IndexOutOfBoundsException("column=" + column);
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    return Boolean.valueOf(configuration.isEventActive(eventTypes[row]));

                case 1:
                    return eventTypes[row].toString();

                default:
                    throw new IndexOutOfBoundsException("column=" + column);
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0;
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            if (column != 0 || !(value instanceof Boolean)) {
                throw new IllegalArgumentException("column=" + column + ", value=" + value);
            }

            configuration.setEvent(eventTypes[row], (Boolean) value);
            this.fireTableCellUpdated(row, column);
        }
    }
}
