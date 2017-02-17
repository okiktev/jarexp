package com.delfin.jarexp.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextAreaHighlighter;

class FilterPanel extends JPanel {

	private static final long serialVersionUID = -4872792789437559170L;

	private static final Logger log = Logger.getLogger(FilterPanel.class.getCanonicalName());

	FilterPanel(final JarTreeSelectionListener listener, final RSyntaxTextArea textArea) {
		super(new BorderLayout());

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				highlight(textArea);
				JSplitPane pane = Content.getSplitPane();
				ContentPanel rightPanel = (ContentPanel) pane.getRightComponent();
				pane.setRightComponent(new ContentPanel(rightPanel.getContent()));
				pane.setDividerLocation(listener.getDividerLocation());
				pane.validate();
				pane.repaint();
			}
		});
		final JTextField searchField = new JTextField();
		final JButton filter = new JButton("Filter");
		filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				highlight(searchField, textArea);
			}
		});
		searchField.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
		        if (e.getKeyCode() == KeyEvent.VK_ENTER){
		        	highlight(searchField, textArea);
		        }
			}
		});

		add(close, BorderLayout.WEST);
		add(searchField, BorderLayout.CENTER);
		add(filter, BorderLayout.EAST);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				searchField.requestFocus();
			}
		});
	}
	
	private static void highlight(JTextField searchField, RSyntaxTextArea textArea) {
		highlight(textArea);
		String token = searchField.getText();
		if (token == null || token.isEmpty()) {
			return;
		}
		String text = textArea.getText().toLowerCase();
		token = token.toLowerCase();
		Highlighter hilit = new RSyntaxTextAreaHighlighter();
		textArea.setHighlighter(hilit);
		for (int i = text.indexOf(token); i >= 0; i = text.indexOf(token, i + 1)) {
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

}
