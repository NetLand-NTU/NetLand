package WidgetsTables;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * @author HAN
 * 
 */
@SuppressWarnings("serial")
public class ScrollImage extends JPanel implements ItemListener {
	private JScrollPane scrollPane;
	private Rule hRule;
	private Rule vRule;
	private BufferedImage image; 

	/**
	 * Create a placeholder icon, which consists in a white box with a black
	 * border and a red x inside. It's used to display something when there are
	 * issues loading an icon from an external location.
	 * 
	 * @author HAN
	 * 
	 */
	class MissingIcon implements Icon {
		private int width = 1000;
		private int height = 50;

		public void paintIcon(Component c, Graphics g, int x, int y) {
			// TODO Auto-generated method stub
			Graphics2D g2 = (Graphics2D) g;
			Shape rect = new Rectangle2D.Double(x + 1, y + 1, width - 2,
					height - 2);
			g2.setColor(Color.WHITE);
			g2.fill(rect);
			g2.setColor(Color.BLACK);
			g2.draw(rect);// By default, the stroke is 1.0f solid line.

			g2.setColor(Color.RED);
			BasicStroke stroke = new BasicStroke(4.0f);
			g2.setStroke(stroke);
			g2.draw(new Line2D.Double(x + 10, y + 10, x + width - 10, y
					+ height - 10));
			g2.draw(new Line2D.Double(x + 10, y + height - 10, x + width - 10,
					y + 10));
		}

		public int getIconWidth() {
			// TODO Auto-generated method stub
			return width;
		}

		public int getIconHeight() {
			// TODO Auto-generated method stub
			return height;
		}
	}

	public class Corner extends JComponent {
		@Override
		protected void paintComponent(Graphics g) {
			g.setColor(new Color(230, 163, 4));
			g.fillRect(0, 0, getWidth(), getHeight());
		}
	}

	/**
	 * The enum type is more powerful than the traditional constants defining by
	 * encapsulate them in an interface.
	 * 
	 * @author HAN
	 * 
	 */
	enum RuleConstants {
		HORIZONTAL, VERTICAL
	}

	public class Rule extends JComponent {
		private RuleConstants direction;
		private String mode;

		// Define the distance on screen for an inch and a centimeter.
		final int INCH = Toolkit.getDefaultToolkit().getScreenResolution();
		final int CM = (int) (INCH / 2.54);

		private int preferredWidth;
		private int preferredHeight;

		private static final int SIZE = 35;

		Rule(RuleConstants direction, String mode) {
			this.direction = direction;
			this.mode = mode;
		}

		private void setUnitMode(String mode) {
			this.mode = mode;
		}

		private int getIncrement() {
			if (mode.equals("cm")) {
				return CM;
			} else if (mode.equals("inch")) {
				return INCH / 2;
			} else {
				return 0;
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			Rectangle currentClip;
			String text;
			Font defaultFont = getFont();

			switch (direction) {
			case HORIZONTAL:
				// Set clip to draw only the visible part of the rule, to ensure
				// speedy scrolling.
				currentClip = new Rectangle(scrollPane.getViewport()
						.getViewPosition().x, 0, scrollPane.getViewport()
						.getExtentSize().width, SIZE);
				g.setClip(currentClip);
				// System.out.println("HORIZONTAL: " + currentClip);

				// Fill this component with a background color.
				g.setColor(new Color(230, 163, 4));
				g.fillRect(currentClip.x, currentClip.y, currentClip.width,
						currentClip.height);

				// Draw ticks and labels.
				g.setColor(Color.BLACK);
				if (mode.equals("cm")) {
					int start;
					if (currentClip.x % CM == 0)
						start = currentClip.x;
					else
						start = (currentClip.x / CM + 1) * CM;
					for (int i = start; i < currentClip.x + currentClip.width; i += CM) {
						// Draw ticks.
						g.drawLine(i, currentClip.height, i,
								currentClip.height - 10);

						// Draw the label.
						Graphics2D g2 = (Graphics2D) g;
						if (i == 0) {
							// Draw particularly the first "0" label.
							text = "0 cm";
							LineMetrics lineMetrics = defaultFont
									.getLineMetrics(text,
											g2.getFontRenderContext());
							g2.drawString(
									text,
									0,
									(float) (currentClip.height - 10 - 2 - lineMetrics
											.getDescent()));
						} else {
							// Draw the other labels.
							text = Integer.toString(i / CM);
							Rectangle2D rect = defaultFont.getStringBounds(
									text, g2.getFontRenderContext());
							LineMetrics lineMetrics = defaultFont
									.getLineMetrics(text,
											g2.getFontRenderContext());
							g2.drawString(
									text,
									(float) (i - rect.getWidth() / 2),
									(float) (currentClip.height - 10 - 2 - lineMetrics
											.getDescent()));
						}
					}

				} else if (mode.equals("inch")) {
					int start;
					if (currentClip.x % (INCH / 2) == 0)
						start = currentClip.x;
					else
						start = (currentClip.x / (INCH / 2) + 1) * (INCH / 2);
					for (int i = start; i < currentClip.x + currentClip.width; i += INCH / 2) {
						if ((i / (INCH / 2)) % 2 == 0) {
							g.drawLine(i, currentClip.height, i,
									currentClip.height - 10);

							// Draw the label.
							Graphics2D g2 = (Graphics2D) g;
							if (i == 0) {
								// Draw particularly the first "0" label.
								text = "0 in";
								LineMetrics lineMetrics = defaultFont
										.getLineMetrics(text,
												g2.getFontRenderContext());
								g2.drawString(
										text,
										0,
										(float) (currentClip.height - 10 - 2 - lineMetrics
												.getDescent()));
							} else {
								// Draw the other labels.
								text = Integer.toString(i / (INCH / 2) / 2);
								Rectangle2D rect = defaultFont.getStringBounds(
										text, g2.getFontRenderContext());
								LineMetrics lineMetrics = defaultFont
										.getLineMetrics(text,
												g2.getFontRenderContext());
								g2.drawString(
										text,
										(float) (i - rect.getWidth() / 2),
										(float) (currentClip.height - 10 - 2 - lineMetrics
												.getDescent()));
							}
						} else {
							g.drawLine(i, currentClip.height, i,
									currentClip.height - 7);
						}
					}
				}
				break;
			case VERTICAL:
				// Set clip to draw only the visible part of the rule, to ensure
				// speedy scrolling.
				currentClip = new Rectangle(0, scrollPane.getViewport()
						.getViewPosition().y, SIZE, scrollPane.getViewport()
						.getExtentSize().height);
				g.setClip(currentClip);
				// System.out.println("VERTICAL: " + currentClip);

				// Fill this component with a background color.
				g.setColor(new Color(230, 163, 4));
				g.fillRect(currentClip.x, currentClip.y, currentClip.width,
						currentClip.height);

				// Draw ticks and labels.
				g.setColor(Color.BLACK);
				if (mode.equals("cm")) {
					int start;
					if (currentClip.y % CM == 0)
						start = currentClip.y;
					else
						start = (currentClip.y / (CM) + 1) * (CM);
					for (int i = start; i < currentClip.y + currentClip.height; i += CM) {
						g.drawLine(currentClip.width, i,
								currentClip.width - 10, i);

						// Draw the label.
						Graphics2D g2 = (Graphics2D) g;
						if (i == 0) {
							// Draw particularly the first "0" label.
							text = "0 cm";
							Rectangle2D rect = defaultFont.getStringBounds(
									text, g2.getFontRenderContext());
							LineMetrics lineMetrics = defaultFont
									.getLineMetrics(text,
											g2.getFontRenderContext());
							g2.drawString(text,
									(float) (currentClip.width - 2 - rect
											.getWidth()), (float) (lineMetrics
											.getAscent()));
						} else {
							// Draw the other labels.
							text = Integer.toString(i / CM);
							Rectangle2D rect = defaultFont.getStringBounds(
									text, g2.getFontRenderContext());
							LineMetrics lineMetrics = defaultFont
									.getLineMetrics(text,
											g2.getFontRenderContext());
							g2.drawString(
									text,
									(float) (currentClip.width - 10 - 2 - rect
											.getWidth()),
									(float) (i + ((lineMetrics.getAscent() + lineMetrics
											.getDescent()) / 2 - lineMetrics
											.getDescent())));
						}
					}

				} else if (mode.equals("inch")) {
					int start;
					if (currentClip.y % (INCH / 2) == 0)
						start = currentClip.y;
					else
						start = (currentClip.y / (INCH / 2) + 1) * (INCH / 2);
					for (int i = start; i < currentClip.y + currentClip.height; i += INCH / 2) {
						if ((i / (INCH / 2)) % 2 == 0) {
							g.drawLine(currentClip.width, i,
									currentClip.width - 10, i);

							// Draw the label.
							Graphics2D g2 = (Graphics2D) g;
							if (i == 0) {
								// Draw particularly the first "0" label.
								text = "0 in";
								Rectangle2D rect = defaultFont.getStringBounds(
										text, g2.getFontRenderContext());
								LineMetrics lineMetrics = defaultFont
										.getLineMetrics(text,
												g2.getFontRenderContext());
								g2.drawString(text,
										(float) (currentClip.width - 2 - rect
												.getWidth()),
										(float) (lineMetrics.getAscent()));
							} else {
								// Draw the other labels.
								text = Integer.toString(i / (INCH / 2) / 2);
								Rectangle2D rect = defaultFont.getStringBounds(
										text, g2.getFontRenderContext());
								LineMetrics lineMetrics = defaultFont
										.getLineMetrics(text,
												g2.getFontRenderContext());
								g2.drawString(
										text,
										(float) (currentClip.width - 10 - 2 - rect
												.getWidth()),
										(float) (i + ((lineMetrics.getAscent() + lineMetrics
												.getDescent()) / 2 - lineMetrics
												.getDescent())));
							}
						} else {
							g.drawLine(currentClip.width, i,
									currentClip.width - 7, i);
						}
					}
				}
				break;
			}
		}

		@Override
		public boolean isOpaque() {
			return true;
		}

		@Override
		public Dimension getPreferredSize() {
			if (direction == RuleConstants.HORIZONTAL) {
				return new Dimension(preferredWidth, SIZE);
			} else if (direction == RuleConstants.VERTICAL) {
				return new Dimension(SIZE, preferredHeight);
			} else {
				return null;
			}
		}

		private void setPreferredWidth(int preferredWidth) {
			this.preferredWidth = preferredWidth;
		}

		private void setPreferredHeight(int preferredHeight) {
			this.preferredHeight = preferredHeight;
		}
	}

	public class PictureView extends JLabel implements Scrollable,
			MouseMotionListener {
		private int increment;

		PictureView(Icon picture, int increment) {
			super(picture);
			this.increment = increment;

			// Sets the autoscrolls property. If true mouse dragged events will
			// be synthetically generated when the mouse is dragged outside of
			// the component's bounds and mouse motion has paused (while the
			// button continues to be held down). The synthetic events make it
			// appear that the drag gesture has resumed in the direction
			// established when the component's boundary was crossed.
			setAutoscrolls(true);

			addMouseMotionListener(this);
		}

		public void mouseDragged(MouseEvent e) {
			// Let picture view scroll automatically when mouse drags out of the
			// bounds of viewport and stops.
			Rectangle rect = new Rectangle(e.getX(), e.getY(), 1, 1);
			// Forwards the scrollRectToVisible() message to the JComponent's
			// parent. Components that can service the request, such as
			// JViewport, override this method and perform the scrolling.
			scrollRectToVisible(rect);
		}

		public void mouseMoved(MouseEvent e) {
		}

		public Dimension getPreferredScrollableViewportSize() {
			return new Dimension(970, 45);
		}

		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			if (orientation == SwingConstants.HORIZONTAL) {
				int x = visibleRect.x;
				if (x % increment != 0) {
					if (direction < 0) {
						return x - (x / increment) * increment;
					} else {
						return (x / increment + 1) * increment - x;
					}
				} else {
					return increment;
				}
			} else if (orientation == SwingConstants.VERTICAL) {
				int y = visibleRect.y;
				if (y % increment != 0) {
					if (direction < 0) {
						return y - (y / increment) * increment;
					} else {
						return (y / increment + 1) * increment - y;
					}
				} else {
					return increment;
				}
			}
			return 0;
		}

		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			if (orientation == SwingConstants.HORIZONTAL) {
				return visibleRect.width - increment;
			} else if (orientation == SwingConstants.VERTICAL) {
				return visibleRect.height - increment;
			}
			return 0;
		}

		public boolean getScrollableTracksViewportWidth() {
			return false;
		}

		public boolean getScrollableTracksViewportHeight() {
			return false;
		}

		private void setIncrement(int increment) {
			this.increment = increment;
		}
	}

	public ScrollImage(BufferedImage image) {		
		// The constructor serves also as a content pane.
		super(new BorderLayout());
		

		this.image = image;
		// Load picture.
		Icon picture;
//		BufferedImage image = createImage("a.jpg");
		if (image == null) {
			picture = new MissingIcon();
		} else {
			picture = new ImageIcon(image, "a");
		}

		// Create and set up the horizontal and vertical rules.
		hRule = new Rule(RuleConstants.HORIZONTAL, "cm");
		vRule = new Rule(RuleConstants.VERTICAL, "cm");
		// The scroll pane puts the row and column headers in JViewPorts of
		// their own. Thus, when scrolling horizontally, the column header
		// follows along, and when scrolling vertically, the row header follows
		// along. Make sure the row and column have the same width and height as
		// the view (if the scroll pane has set the viewport border, the
		// border's size should be taken into account), because JScrollPane does
		// not enforce these values to have the same size. If one differs from
		// the other, you are likely to not get the desired behavior.
		hRule.setPreferredWidth(picture.getIconWidth());
		vRule.setPreferredHeight(picture.getIconHeight());

//		// Create the upper-left corner.
//		JToggleButton toggleButton = new JToggleButton("cm", true);
//		toggleButton.addItemListener(this);
//		toggleButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
//		toggleButton.setMargin(new Insets(2, 2, 2, 2));
//		JPanel upperLeftCorner = new JPanel();
//		upperLeftCorner.add(toggleButton);
//
//		// Create the upper-right corner.
//		Corner upperRightCorner = new Corner();
//
//		// Create the lower-right corner.
//		Corner lowerLeftCorner = new Corner();

		// Create the picture view as the client of the scroll pane.
		PictureView pictureView = new PictureView(picture, hRule.getIncrement());

		// Create and set up the scroll pane.
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(pictureView);
//		scrollPane.setRowHeaderView(vRule);
//		scrollPane.setColumnHeaderView(hRule);
//		scrollPane.setCorner(ScrollPaneConstants.UPPER_LEFT_CORNER,
//				upperLeftCorner);
//		scrollPane.setCorner(ScrollPaneConstants.UPPER_RIGHT_CORNER,
//				upperRightCorner);
//		scrollPane.setCorner(ScrollPaneConstants.LOWER_LEFT_CORNER,
//				lowerLeftCorner);

		// Layout the content pane.
		add(scrollPane, BorderLayout.CENTER);
	}

	

	public void itemStateChanged(ItemEvent e) {
		// Set the unit mode: cm or inch.
		if (e.getStateChange() == ItemEvent.SELECTED) {
			hRule.setUnitMode("cm");
			vRule.setUnitMode("cm");
		} else {
			hRule.setUnitMode("inch");
			vRule.setUnitMode("inch");
		}
		hRule.repaint();
		vRule.repaint();

		// Notify the scrollable the new increment.
		((PictureView) scrollPane.getViewport().getView()).setIncrement(hRule
				.getIncrement());
	}
	
	
	public BufferedImage getImage(){
		return image;
	}
	
	public void setImage(BufferedImage img){
		this.image = img;
	}

}
