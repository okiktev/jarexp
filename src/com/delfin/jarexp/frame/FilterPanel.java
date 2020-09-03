package com.delfin.jarexp.frame;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.NORTH;
import static java.awt.GridBagConstraints.SOUTH;
import static java.awt.GridBagConstraints.WEST;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaHighlighter;

import com.delfin.jarexp.exception.JarexpException;
import com.delfin.jarexp.settings.Settings;
import com.delfin.jarexp.utils.StringUtils;

class FilterPanel extends JPanel {

	private static final long serialVersionUID = -1086745670394347474L;

	private static final Logger log = Logger.getLogger(FilterPanel.class.getCanonicalName());

	static final DefaultHighlightPainter DEFAULT_HIGHLIGHT_PAINTER = new DefaultHighlightPainter(Color.LIGHT_GRAY);

	private JButton btClose = new JButton("\u2A2F");
	private JTextField tfSearch = new JTextField();
	private BasicArrowButton btUp = new BasicArrowButton(BasicArrowButton.NORTH, null, null, Color.BLACK, null);
	private BasicArrowButton btDown = new BasicArrowButton(BasicArrowButton.SOUTH, null, null, Color.BLACK, null);

	private int currPosition;
	private JarTreeSelectionListener listener;

	public FilterPanel(JarTreeSelectionListener listener) {
		super();

		this.listener = listener;

		initComponents();
		alignComponents();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tfSearch.requestFocus();
			}
		});
	}

	private void alignComponents() {
		setLayout(new GridBagLayout());

		Insets insets = new Insets(0, 0, 0, 0);
		add(btUp, new GridBagConstraints(0, 0, 1, 1, 0, 0, NORTH, NONE, insets, 0, 0));
		add(btDown, new GridBagConstraints(0, 1, 1, 1, 0, 0, SOUTH, NONE, insets, 0, 0));
		add(tfSearch, new GridBagConstraints(1, 0, 1, 2, 1, 1, WEST, BOTH, insets, 0, 0));
		add(btClose, new GridBagConstraints(2, 0, 1, 2, 0, 0, NORTH, NONE, insets, 0, 0));
	}

	private void initComponents() {

		btDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String token = tfSearch.getText();
				if (token == null || token.isEmpty()) {
					return;
				}
				String text = getTextArea().getText();
				currPosition = StringUtils.indexOf(text, token, currPosition + 1);
				if (currPosition == -1) {
					currPosition = 0;
					currPosition = StringUtils.indexOf(text, token, currPosition + 1);
				}
				scrollToVisible(getTextArea());
			}
		});

		btUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String token = tfSearch.getText();
				if (token == null || token.isEmpty()) {
					return;
				}
				String text = getTextArea().getText();
				currPosition = StringUtils.lastIndexOf(text, token, currPosition - 1);
				if (currPosition == -1) {
					currPosition = 0;
					currPosition = StringUtils.lastIndexOf(text, token, currPosition - 1);
				}
				scrollToVisible(getTextArea());
			}
		});

		btClose.setPreferredSize(new Dimension(16, 16));
		btClose.setBorder(BorderFactory.createEmptyBorder());
		btClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				highlight(getTextArea());
				JSplitPane pane = Content.getSplitPane();
				ContentPanel rightPanel = (ContentPanel) pane.getRightComponent();
				pane.setDividerLocation(listener.getDividerLocation());
				pane.setRightComponent(new ContentPanel(rightPanel.getContent()));
				pane.validate();
				pane.repaint();
			}
		});

		Font font = tfSearch.getFont();
		tfSearch.setFont(new Font(Settings.DLG_TEXT_FONT.getName(), font.getStyle(), 18));
		tfSearch.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_ENTER:
					btDown.doClick(100);
					highlight(tfSearch, getTextArea());
					break;
				case KeyEvent.VK_ESCAPE:
					btClose.doClick(100);
					break;
				}
			}
		});

		Content.getSplitPane().getRootPane().setDefaultButton(btDown);
	}
	
	private RSyntaxTextArea getTextArea() {
		return (RSyntaxTextArea)listener.area;
	}

	private void scrollToVisible(RSyntaxTextArea textArea) {
		try {
			textArea.scrollRectToVisible(textArea.modelToView(currPosition));
		} catch (BadLocationException e) {
			throw new JarexpException("Could not scroll to found index.", e);
		}
	}

	private static void highlight(JTextField searchField, RSyntaxTextArea textArea) {
		highlight(textArea);
		String token = searchField.getText();
		if (token == null || token.isEmpty()) {
			return;
		}
		String text = textArea.getText();
		Highlighter hilit = new RSyntaxTextAreaHighlighter();
		textArea.setHighlighter(hilit);
		for (int i = StringUtils.indexOf(text, token); i >= 0; i = StringUtils.indexOf(text, token, i + 1)) {
			try {
				hilit.addHighlight(i, i + token.length(), new DefaultHighlightPainter(Color.LIGHT_GRAY));
			} catch (BadLocationException e) {
				log.log(Level.WARNING, "An error occurred while highlighting token " + token, e);
			}
		}
	}

	private static void highlight(RSyntaxTextArea textArea) {
		textArea.setHighlighter(new RSyntaxTextAreaHighlighter());
	}

	public static void main(String[] args) throws Exception {
		FilterPanel center = new FilterPanel(null);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(center, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

}
