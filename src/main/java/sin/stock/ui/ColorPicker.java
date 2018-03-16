package sin.stock.ui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sin.glouds.util.MouseUtil;

public class ColorPicker extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Color borderColor = new Color(190, 20, 10);
	private static Color priceColor = new Color(190, 20, 10);
	private static Color nodeColor = new Color(132, 0, 0);
	private static int initBorderY = 690, initPriceOriginX = 500;
	private int timeBeginX, timeEndX, timeInitY, priceInitY, priceTop, priceBottom;
	private boolean initFlag = false;
	JTextArea textArea;
	private int hours = 24, minutes;
	private int hourBegin = 5;
	private int hourLength, minuteLength;
	private StockDataAnalyzer analyzer = StockDataAnalyzer.getInstance();

	public ColorPicker() {
		textArea = new JTextArea();
		textArea.setRows(5);
		textArea.setColumns(20);
		textArea.setEditable(true);
		textArea.append("---系统启动---");
		JScrollPane scrollPane = new JScrollPane(textArea);
		JButton button = new JButton("初始化");
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					textArea.append("\n" + "开始初始化时间轴。。。");
					setVisible(false);
//					Thread.sleep(1000);
//					Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
//					Rectangle rectangle = new Rectangle(0, 0, dimension.width, dimension.height);
//					Robot robot = new Robot();
//					BufferedImage bi = robot.createScreenCapture(rectangle);
//					ImageIO.write(bi, "jpg", new File("H:/temp/stock.jpg"));
					BufferedImage bi = ImageIO.read(new File("H:/temp/stock.jpg"));
					if (initTimeInterval(bi)) {
						textArea.append("\n初始化时间轴成功！");
						textArea.append(
								"时间轴跨度：" + timeBeginX + "," + timeEndX + " 包含" + (timeEndX - timeBeginX) + "像素");
						textArea.append("\n" + "开始初始化时间节点。。。");
						// if (initTimenode(bi)) {
						// initFlag = true;
						// textArea.append("\n初始化时间节点成功！");
						// } else {
						// initFlag = false;
						// textArea.append("\n初始化时间节点失败！");
						// textArea.append("\n初始化失败！");
						// }
						// 暂时初始化时间节点的方法
						if (initTimenodeTemp()) {
							textArea.append("\n初始化时间节点成功！精确度：" + (60 / minutes) + "分。");
							textArea.append("\n开始初始化价格原点");
							if (initPriceOrigin(bi)) {
								textArea.append("\n初始化价格原点成功");
								textArea.append("\n开始加载当前数据");
								if (loadCurrData(bi)) {
									textArea.append("\n初始化当前数据成功");
									analyzer.printData();
								} else {
									textArea.append("\n初始化当前数据失败！");
									textArea.append("\n初始化失败！");
								}
							} else {
								textArea.append("\n初始化价格原点失败！");
								textArea.append("\n初始化失败！");
							}
						} else {
							textArea.append("\n初始化时间节点失败！");
							textArea.append("\n初始化失败！");
						}
					} else {
						initFlag = false;
						textArea.append("\n初始化时间轴失败！");
						textArea.append("\n初始化失败！");
					}
					setVisible(true);
				} catch (Exception ex) {
					ex.printStackTrace();
					textArea.append("\n初始化失败，发生了未知的错误");
				}
			}
		});
		setBackground(new Color(123, 123, 12));
		setLayout(new FlowLayout());
		add(button);
		add(scrollPane);
		setBounds((int) Toolkit.getDefaultToolkit().getScreenSize().width - 300,
				(int) Toolkit.getDefaultToolkit().getScreenSize().height - 400, 300, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	public boolean initTimeInterval(BufferedImage bi) {
		if (Desktop.isDesktopSupported()) {
			List<Integer> pointx = new ArrayList<>();
			fillHorColorPoint(pointx, initBorderY, bi, borderColor);
			timeInitY = initBorderY;
			if (pointx.size() != 4) {
				timeInitY = initBorderY + 10;
				fillHorColorPoint(pointx, initBorderY + 10, bi, borderColor);
			}
			if (pointx.size() == 4) {
				timeBeginX = pointx.get(0);
				timeEndX = pointx.get(1);
				return true;
			}
		}
		return false;
	}

	private static void fillHorColorPoint(List<Integer> points, int y, BufferedImage bi, Color color) {
//		System.out.println("开始填充颜色点,Y轴坐标:" + y);
		points.clear();
		for (int i = 0; i < bi.getWidth(); i++) {
			int rgb = color.getRGB();
//			System.out.println("校验点位:" + i + "," + y + ">>>" + rgb + " " + bi.getRGB(i, y));
			if (rgb == bi.getRGB(i, y)) {
				points.add(i);
			}
		}
//		System.out.println("颜色点数：" + points.size());
	}

	private static void fillVerColorPoint(List<Integer> points, int x, BufferedImage bi, Color color) {
		// System.out.println("开始填充颜色点,Y轴坐标:" + y);
		points.clear();
		for (int i = 0; i < bi.getHeight(); i++) {
			// System.out.println("校验点位:" + i + "," + y);
			int rgb = color.getRGB();
			if (rgb == bi.getRGB(x, i)) {
				points.add(i);
			}
		}
		// System.out.println("颜色点数：" + points.size());
	}

	private boolean initTimenode(BufferedImage bi) {
		List<Integer> timenodes = new ArrayList<>();
		for (int i = timeBeginX; i < timeEndX; i++) {
			Color color = new Color(bi.getRGB(i, timeInitY));
			// Color color = ColorUtil.getColor(i, timeInitY);
			System.out.println(i + "," + timeInitY + " " + color);
			MouseUtil.moveTo(i, timeInitY);
			if (color.equals(nodeColor)) {
				timenodes.add(i);
			}
		}
		if (timenodes.size() > 0)
			return true;
		return false;
	}

	private boolean initTimenodeTemp() {
		int timeLength = timeEndX - timeBeginX;
		if (timeLength > hours) {
			hourLength = timeLength / hours;
			if (hourLength > 60) {
				minuteLength = hourLength / 60;
				minutes = 60;
			} else if (hourLength > 30) {
				minuteLength = hourLength / 30;
				minutes = 30;
			} else if (hourLength > 20) {
				minuteLength = hourLength / 20;
				minutes = 20;
			} else if (hourLength > 10) {
				minuteLength = hourLength / 10;
				minutes = 10;
			} else {
				return false;
			}
			return true;
		}
		return false;
	}

	private boolean loadCurrData(BufferedImage bi) {
		Calendar calendar = new GregorianCalendar();
		int currHour = calendar.get(Calendar.HOUR_OF_DAY);
		int top = priceInitY, bottom = priceInitY;
		if (currHour < hourBegin) {
			textArea.append("\n未到开盘时间");
		} else {
			analyzer.putData("0500", new ArrayList<>());
			for (int i = 0; i < currHour - hourBegin + 1; i++) {
				for (int j = 0; j < minutes; j++) {
					int dataX = timeBeginX + i * hourLength + j * hourLength / minutes;
					List<Integer> data = new ArrayList<>();
					System.out.println("init dataX:" + dataX);
					if (isData(bi.getRGB(dataX, top))) {
						data.add(top);
						for (int t = top - 1; t > priceTop; t--) {
							if (isData(bi.getRGB(dataX, t))) {
								data.add(t);
							} else {
								top = t;
								break;
							}
						}
						for (int b = top + 1; b < priceBottom; b++) {
							if (isData(bi.getRGB(dataX, b))) {
								data.add(b);
							} else {
								bottom = b;
								break;
							}
						}
					} else if (isData(bottom)) {
						data.add(bottom);
						for (int t = bottom - 1; t > top; t--) {
							if (isData(bi.getRGB(dataX, t))) {
								data.add(t);
							} else {
								top = t;
								break;
							}
						}
						for (int b = bottom + 1; b < priceBottom; b++) {
							if (isData(bi.getRGB(dataX, b))) {
								data.add(b);
							} else {
								bottom = b;
								break;
							}
						}
					} else {
						boolean flag = false;
						for (int t = priceTop; t < priceBottom; t++) {
							if (isData(bi.getRGB(dataX, t))) {
								if (!flag) {
									flag = true;
									top = t;
								}
								data.add(t);
							} else if (flag) {
								bottom = t - 1;
							}
						}
					}
					int currMinute = j * 60 / minutes;
					int curHour = hourBegin + i;
					String currTime = "" + (curHour > 9 ? curHour : "0" + curHour)
							+ (currMinute > 9 ? currMinute : "0" + currMinute);
					analyzer.putData(currTime, data);
				}
			}
			return true;
		}
		return false;
	}

	private boolean initPriceOrigin(BufferedImage bi) {
		if (Desktop.isDesktopSupported()) {
			List<Integer> pointy = new ArrayList<>();
			fillVerColorPoint(pointy, initPriceOriginX, bi, priceColor);
			if (pointy.size() != 4) {
				fillVerColorPoint(pointy, initPriceOriginX + 10, bi, priceColor);
			}
			if (pointy.size() == 4) {
				priceInitY = pointy.get(1);
				priceBottom = pointy.get(2);
				priceTop = pointy.get(0);
				return true;
			}
		}
		return false;
	}

	private boolean isData(int rgb) {
		Color color = new Color(rgb);
		if(color.getRed() != 0 && color.getRed() == color.getBlue() && color.getRed() == color.getBlue())
			return true;
		return false;
	}
}
