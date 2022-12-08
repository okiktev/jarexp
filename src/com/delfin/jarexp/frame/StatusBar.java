package com.delfin.jarexp.frame;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicProgressBarUI;

import com.delfin.jarexp.decompiler.Decompiler.DecompilerType;
import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.frame.resources.Resources;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.ImgPanel;

class StatusBar extends JStatusBar {

	private static final long serialVersionUID = 550207775529991234L;
	private static final Resources RESOURCES = Resources.getInstance();
	private JProgressBar progressBar = new JProgressBar();
	private final Content content;
	private final JLabel path = new JLabel("");
	private final JLabel compJava = new JLabel("");
	private final JLabel children = new JLabel("");
	private JPanel decompilerImg;
	private DecompilerType decompilerType;

	StatusBar(Content content) {
		super();
		this.content = content;

		compJava.setToolTipText("Version of Java which was class compiled with");
		children.setToolTipText("Number of all objects placed in current folder/archive");

		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		progressBar.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Panel.background")));
		progressBar.setUI(new BasicProgressBarUI());

		JPanel panel = new JPanel(new GridLayout(1, 1));
		panel.add(progressBar);

		setDecompiler(Settings.getDecompilerType());

		add(new StatusBarItem("children", children, "20"));
		add(new StatusBarItem("comp_java", compJava, "20"));
		add(new StatusBarItem("place_holder", path, "*"));
		add(new StatusBarItem("decompiler_ico", decompilerImg, "20"));
		add(new StatusBarItem("progress_bar", panel, "80"));
	}

	void enableProgress(String msg) {
		content.setCursor(new Cursor(Cursor.WAIT_CURSOR));
		progressBar.setString(msg);
		progressBar.setIndeterminate(true);
		progressBar.setVisible(true);
	}

	void disableProgress() {
		content.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		progressBar.setVisible(false);
	}

	void setCompiledVersion(String ver) {
		compJava.setText(ver);
	}

	void setChildren(String count) {
		children.setText(count);
	}

	void setPath(String path) {
		this.path.setText(path);
	}

	String getCompiledVersion() {
		return compJava.getText();
	}

	String getChildren() {
		return children.getText();
	}

	String getPath() {
		return path.getText();
	}

	DecompilerType getDecompilerType() {
		return decompilerType;
	}

	void setDecompiler(DecompilerType decompiler) {
		Image img;
		switch (decompiler) {
		case JDCORE:
			img = RESOURCES.getJdCoreImage();
			break;
		case FERNFLOWER:
			img = RESOURCES.getFernflowerImage();
			break;
		case PROCYON:
			img = RESOURCES.getProcyonImage();
			break;
		default:
			throw new JarexpException("Unable to define decompiler type " + decompiler);
		}
		if (decompilerImg == null) {
			decompilerImg = new ImgPanel(img);
		} else {			
			((ImgPanel)decompilerImg).setImage(img);
		}
		decompilerImg.repaint();
		this.decompilerType = decompiler;
	}

	public void empty() {
		path.setText("");
		compJava.setText("");
		children.setText("");
	}

}

class JStatusBar extends JComponent {

	static class StatusBarItem {

		String id;
		Component comp;
		String constraint;

		public StatusBarItem(String id, Component comp, String constraint) {
			this.id = id;
			this.comp = comp;
			this.constraint = constraint;
		}

	}

	private static final long serialVersionUID = 8658346171005319162L;

	private Map<String, Component> items;

	JStatusBar() {
		setLayout(new LinearLayout(LinearLayout.HORIZONTAL, 0));
		items = new HashMap<String, Component>();
	}

	public void add(StatusBarItem item) {
		Component comp = items.get(item.id);
		if (comp != null) {
			remove(comp);
			items.remove(item.id);
		}
		((JComponent) item.comp).setBorder(BorderFactory.createLineBorder(Color.lightGray));
		add(item.comp, item.constraint);
		items.put(item.id, comp);
	}

}

class LinearLayout implements LayoutManager2 {

	private static class Constraint {
		protected Object value;

		private Constraint(Object value) {
			this.value = value;
		}
	}

	private static class NumberConstraint extends Constraint {
		public NumberConstraint(int d) {
			this(new Integer(d));
		}

		public NumberConstraint(Integer d) {
			super(d);
		}

		public int intValue() {
			return ((Integer) value).intValue();
		}
	}

	private static class PercentConstraint extends Constraint {
		public PercentConstraint(float d) {
			super(new Float(d));
		}

		public float floatValue() {
			return ((Float) value).floatValue();
		}
	}

	final static int HORIZONTAL = 0;
	final static int VERTICAL = 1;
	private final static Constraint REMAINING_SPACE = new Constraint("*");
	private final static Constraint PREFERRED_SIZE = new Constraint("");

	private int orientation;
	private int indent;
	private Map<Component, Object> constraints;

	LinearLayout(int orientation, int indent) {
		if (orientation != HORIZONTAL && orientation != VERTICAL) {
			throw new IllegalArgumentException("Orientation must be one of HORIZONTAL or VERTICAL");
		}
		this.orientation = orientation;
		this.indent = indent;
		constraints = new HashMap<Component, Object>();
	}

	@Override
	public void addLayoutComponent(String name, Component comp) {
		throw new RuntimeException("is not implemented");
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		constraints.remove(comp);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		int width = 0;
		int height = 0;
		boolean isFirst = true;
		for (Component comp : parent.getComponents()) {
			Dimension size = comp.getPreferredSize();
			if (orientation == HORIZONTAL) {
				height = Math.max(height, size.height);
				width += size.width;
				if (isFirst) {
					isFirst = false;
				} else {
					width += indent;
				}
			} else {
				height += size.height;
				width = Math.max(width, size.width);
				if (isFirst) {
					isFirst = false;
				} else {
					height += indent;
				}
			}
		}
		Insets insets = parent.getInsets();
		return new Dimension(width + insets.right + insets.left, height + insets.top + insets.bottom);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return preferredLayoutSize(parent);
	}

	@Override
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		Dimension size = parent.getSize();
		size.width = size.width - insets.left - insets.right;
		size.height = size.height - insets.top - insets.bottom;

		Component[] components = parent.getComponents();

		int[] sizes = new int[components.length];
		int availableSize = (HORIZONTAL == orientation ? size.width : size.height) - (components.length - 1) * indent;

		for (int i = 0; i < components.length; ++i) {
			Constraint constraint = (Constraint) constraints.get(components[i]);
			if (constraint == null || PREFERRED_SIZE.equals(constraint)) {
				Dimension prefSize = components[i].getPreferredSize();
				sizes[i] = HORIZONTAL == orientation ? prefSize.width : prefSize.height;
				availableSize -= sizes[i];
			} else if (constraint instanceof NumberConstraint) {
				sizes[i] = ((NumberConstraint) constraint).intValue();
				availableSize -= sizes[i];
			}
		}

		int remainingSize = availableSize;
		for (int i = 0; i < components.length; ++i) {
			Constraint constraint = (Constraint) constraints.get(components[i]);
			if (constraint instanceof PercentConstraint) {
				sizes[i] = (int) (remainingSize * ((PercentConstraint) constraint).floatValue());
				availableSize -= sizes[i];
			}
		}

		List<Integer> remaining = new ArrayList<Integer>();
		for (int i = 0; i < components.length; ++i) {
			Constraint constraint = (Constraint) constraints.get(components[i]);
			if (REMAINING_SPACE.equals(constraint)) {
				remaining.add(new Integer(i));
				sizes[i] = 0;
			}
		}

		if (!remaining.isEmpty()) {
			int rest = availableSize / remaining.size();
			for (Iterator<Integer> iter = remaining.iterator(); iter.hasNext();) {
				sizes[((Integer) iter.next()).intValue()] = rest;
			}
		}

		int currentOffset = HORIZONTAL == orientation ? insets.left : insets.top;
		for (int i = 0; i < components.length; ++i) {
			if (HORIZONTAL == orientation) {
				int width = sizes[i];
				if (i == components.length - 1) {
					width += 2;
				}
				components[i].setBounds(currentOffset, insets.top, width, size.height);
			} else {
				components[i].setBounds(insets.left, currentOffset, size.width, sizes[i]);
			}
			currentOffset += indent + sizes[i] - 1;
		}
	}

	@Override
	public void addLayoutComponent(Component comp, Object constraints) {
		setConstraint(comp, constraints);
	}

	@Override
	public Dimension maximumLayoutSize(Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public float getLayoutAlignmentX(Container target) {
		return target.getAlignmentX();
		// TODO return 1.0f / 2.0f;
	}

	@Override
	public float getLayoutAlignmentY(Container target) {
		return target.getAlignmentY();
		// TODO return 1.0f / 2.0f;
	}

	@Override
	public void invalidateLayout(Container target) {
		// ignore this implementation
	}

	private void setConstraint(Component component, Object constraint) {
		if (constraint instanceof Constraint) {
			this.constraints.put(component, constraint);
		} else if (constraint instanceof Number) {
			setConstraint(component, new NumberConstraint(((Number) constraint).intValue()));
		} else if ("*".equals(constraint)) {
			setConstraint(component, REMAINING_SPACE);
		} else if (constraint == null || "".equals(constraint)) {
			setConstraint(component, PREFERRED_SIZE);
		} else if (constraint instanceof String) {
			setConstraint(component, parseConstraint(constraint));
		} else {
			throw new IllegalArgumentException("Unexpected constraint provided: " + constraint);
		}
	}

	private static Constraint parseConstraint(Object constraint) {
		String s = (String) constraint;
		if (!s.endsWith("%")) {
			return new NumberConstraint(Integer.valueOf(s));
		}
		float value = Float.valueOf(s.substring(0, s.length() - 1)).floatValue() / 100;
		if (value > 1 || value < 0) {
			throw new IllegalArgumentException("Percent value must be between 0 and 100");
		}
		return new PercentConstraint(value);
	}

}
