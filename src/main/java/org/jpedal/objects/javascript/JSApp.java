/*
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.idrsolutions.com
 * Help section for developers at http://www.idrsolutions.com/support/
 *
 * (C) Copyright 1997-2015 IDRsolutions and Contributors.
 *
 * This file is part of JPedal/JPDF2HTML5
 *
     This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


 *
 * ---------------
 * JSApp.java
 * ---------------
 */
package org.jpedal.objects.javascript;

import org.jpedal.parser.DecoderOptions;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import org.jpedal.utils.Messages;


public class JSApp{

	private static final int ICON_ERROR = 0;
	private static final int ICON_WARNING = 1;
	private static final int ICON_QUESTION = 2;
	private static final int ICON_STATUS = 3;

	private static final int BUTTONTYPE_OK = 0;
	private static final int BUTTONTYPE_OK_CANCEL = 1;
	private static final int BUTTONTYPE_YES_NO = 2;
	private static final int BUTTONTYPE_YES_NO_CANCEL = 3;

	private static final int BUTTON_OK = 1;
	private static final int BUTTON_CANCEL = 2;
	private static final int BUTTON_NO = 3;
	private static final int BUTTON_YES = 4;

    public static final boolean showOutput=false;


    private int count;
//    public Object platform="wwww";

	// Some fields that may be needed
	public String viewerType = "Exchange-Pro"; // Acrobat Professional version 6.0 or later
	public String viewerVariation = "Reader"; // Indicates the packaging of the running viewer application.
	public int viewerVersion = 10;
	public String platform = "UNIX";

	/**
	 * Sets the platform variable to be correct
	 */
	private void setPlatform() {
		if(DecoderOptions.isRunningOnWindows) {
			platform = "WIN";
		}
		else if(DecoderOptions.isRunningOnMac) {
			platform = "MAC";
		}
		else {
			platform = "UNIX";
		}
	}

    public static void alert(){

        if(showOutput) {
            System.out.println("JSApp.alert()");
        }
    }

    public static void alert(final Object objA, final Object objB, final Object objC){

        if(showOutput) {
            System.out.println("JSApp.alert(objA, objB, objC)");
        }
    }

	/**
	 * Display a message popup
	 * @param cMsg The message text to display
	 * @param nIcon An icon type. Possible values: ICON_ERROR, ICON_WARNING, ICON_QUESTION, ICON_STATUS
	 * @param nType A button group type. Possible values are: BUTTONTYPE_OK, BUTTONTYPE_OK_CANCEL, BUTTONTYPE_YES_NO, BUTTONTYPE_YES_NO_CANCEL
	 * @return nButton The type of button pressed by the user.
	 */
    public static int alert(final String cMsg, final int nIcon, final int nType, String title){

        if(showOutput) {
            System.out.println("JSApp.alert(cMsg, nIcon, nType)");
        }

        // <start-demo><end-demo>

		final int javaIcon;
		final int javaButtonType;

		switch (nIcon) {
			case ICON_ERROR:
				javaIcon = JOptionPane.ERROR_MESSAGE;
				break;
			case ICON_WARNING:
				javaIcon = JOptionPane.WARNING_MESSAGE;
				break;
			case ICON_QUESTION:
				javaIcon = JOptionPane.QUESTION_MESSAGE;
				break;
			case ICON_STATUS:
				javaIcon = JOptionPane.PLAIN_MESSAGE;
				break;
			default:
				javaIcon = JOptionPane.PLAIN_MESSAGE;
		}

		// Map the JavaScript button types onto JOptionPane types
		switch (nType) {
			case BUTTONTYPE_OK:
				javaButtonType = JOptionPane.DEFAULT_OPTION;
				break;
			case BUTTONTYPE_OK_CANCEL:
				javaButtonType = JOptionPane.OK_CANCEL_OPTION;
				break;
			case BUTTONTYPE_YES_NO:
				javaButtonType = JOptionPane.YES_NO_OPTION;
				break;
			case BUTTONTYPE_YES_NO_CANCEL:
				javaButtonType = JOptionPane.YES_NO_CANCEL_OPTION;
				break;
			default:
				javaButtonType = JOptionPane.OK_CANCEL_OPTION;
		}
//		JOptionPane.showConfirmDialog(null, cMsg, "JavaScript Message");
		if(title == null || title.length() <= 0) {
			title = "Jpedal JavaScript Window";
		}
		int nButton = JOptionPane.showConfirmDialog(null, cMsg, title, javaButtonType, javaIcon);

		// Set the correct output type
		switch(nButton) {
			case JOptionPane.OK_OPTION: // or JOptionPane.YES_OPTION:
				// need to check if this was an OK OPTION or one of the YES_NO options
				if(nType == BUTTONTYPE_YES_NO || nType == BUTTONTYPE_YES_NO_CANCEL) {
					nButton = BUTTON_YES;
				}
				else {
					nButton = BUTTON_OK;
				}
				break;
			case JOptionPane.CANCEL_OPTION:
				nButton = BUTTON_CANCEL;
				break;
			case JOptionPane.NO_OPTION:
				nButton = BUTTON_NO;
				break;
			case JOptionPane.CLOSED_OPTION:
				// special case, when user selects the x in the corner of the alert
				if(javaButtonType == JOptionPane.DEFAULT_OPTION) {
					// If it was a default option the result does not matter?
					nButton = BUTTON_OK;
				}
				else if(javaButtonType == JOptionPane.YES_NO_OPTION) {
					// Should be a no when there is no cancel button present
					nButton = BUTTON_NO;
				}
				else {
					// All other cases there is a cancel button present
					nButton = BUTTON_CANCEL;
				}
				break;
		}
        return nButton;
    }

	public static int alert(final String cMsg, final int nIcon, final int nType){
		return alert(cMsg,nIcon,nType,null);
	}
	public static int alert(final String cMsg, final int nIcon){
		return alert(cMsg,nIcon,-1,null);
	}


	// These should show a warning to the user that they are not implmented but still attempt some kind of approximate beahviour
	public static int alert(final String cMsg, final int nIcon, final int nType, final String title, final Object oDoc, final Object oCheckbox){

        if(showOutput) {
            System.err.println("Setting the doc object an alert should be associated with or adding a checkbox is not implemented.");
        }

		return alert(cMsg, nIcon, nType, title);
	}
	public static int alert(final String cMsg, final int nIcon, final int nType, final String title, final Object oDoc){

        if(showOutput) {
            System.err.println("Setting the doc object an alert should be associated with is not implemented.");
        }

		return alert(cMsg, nIcon, nType, title);
	}

	/**
	 * Displays a dialog box containing a question and an entry field for the user to reply to the question.
	 * @param cQuestion The question
	 * @param cTitle The title of the dialog box
	 * @param cDefault The default answer
	 * @param bPassword Whether this is a password box or not
	 * @param cLabel A label next to the entry field
	 * @return The entered text or null if the user clicked cancel.
	 */
	public static String response(final String cQuestion, final String cTitle, final String cDefault, final boolean bPassword, final String cLabel) {
		final BorderLayout layout = new BorderLayout();
		layout.setHgap(5);
		final JPanel panel = new JPanel(layout);
		final JLabel question = new JLabel(cQuestion);

		// Set up the field to type into
		final JTextField field;
		if(bPassword) {
			field = new JPasswordField();
		}
		else {
			field = new JTextField();
		}

		// Set up the default text if there is any
		if(cDefault != null) {
			field.setText(cDefault);
		}

		// Set the title
		final String title;
		if(cTitle != null) {
			title = cTitle;
		}
		else {
			title = "Input";
		}

		// Add the objects to the panel
		panel.add(question, BorderLayout.NORTH);
		if(cLabel != null) {
			final JLabel label = new JLabel(cLabel);
			panel.add(label, BorderLayout.WEST);
		}
		panel.add(field, BorderLayout.CENTER);
		final String[] options = {"Ok", "Cancel"}; // the two options

		final int option = JOptionPane.showOptionDialog(null, panel, title, JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		if(option == 0) {
			return field.getText();
		}
		else {
			return null;
		}
	}

	/**
	 * @see JSApp#response(String, String, String, boolean, String)
	 * @param cQuestion
	 * @param cTitle
	 * @param cDefault
	 * @param bPassword
	 * @return
	 */
	public static String response(final String cQuestion, final String cTitle, final String cDefault, final boolean bPassword) {
		return response(cQuestion, cTitle, cDefault, bPassword, null);
	}

	/**
	 * @see JSApp#response(String, String, String, boolean, String)
	 * @param cQuestion
	 * @param cTitle
	 * @param cDefault
	 * @return
	 */
	public static String response(final String cQuestion, final String cTitle, final String cDefault) {
		return response(cQuestion, cTitle, cDefault, false, null);
	}

	/**
	 * @see JSApp#response(String, String, String, boolean, String)
	 * @param cQuestion
	 * @param cTitle
	 * @return
	 */
	public static String response(final String cQuestion, final String cTitle) {
		return response(cQuestion, cTitle, null, false, null);
//		return JOptionPane.showInputDialog(null, cQuestion, cTitle);
	}

	/**
	 * @see JSApp#response(String, String, String, boolean, String)
	 * @param cQuestion
	 * @return
	 */
	public static String response(final String cQuestion) {
		return response(cQuestion, null, null, false, null);
//		return JOptionPane.showInputDialog(null, cQuestion);
	}

	/**
	 * Displays a message popup
	 * @param cMsg the message text to display
	 * @return nButton The type of button pressed by the user
	 */
    @SuppressWarnings("UnusedReturnValue")
    public static int alert(final String cMsg){

        if(showOutput) {
            System.out.println("JSApp.alert(cMsg)");
        }
		return JOptionPane.showConfirmDialog(null, cMsg, "JavaScript", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE); // no other option
    }

	public static void beep() {

        if(showOutput) {
            System.out.println("JSApp.beep()");
        }

		Toolkit.getDefaultToolkit().beep();
	}
	public static void beep(final int number) {

        if(showOutput) {
            System.out.println("JSApp.beep(" + number + ')');
        }

		Toolkit.getDefaultToolkit().beep();
	}

	public static void launchURL(final String url) {
		try {
            if(showOutput) {
                System.out.println("JSApp.launchURL(" + url + ')');
            }

			java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	public static void launchURL(final String url, final boolean newWindow) {
		launchURL(url);
	}

	public static void popUpMenuEx(final String[] array) {

        if(showOutput) {
            System.out.println("JSApp.popUpMenuEx(" + Arrays.toString(array) + ')');
        }
	}

	/**
	 * Send an email using the default email client.
	 * bUI and cTo are required. The other fields can be null or in javascript, omitted
	 * @param bUI Whether to display the GUI or not (currently ignored)
	 * @param cTo A semicolon separated list of email addresses to send to
	 * @param cCc A semicolon separated list of email addresses to CC to
	 * @param cBcc A semicolon separated list of email addresses to BCC to
	 * @param cSubject The subject line text
	 * @param cMsg The message text
	 */
	@SuppressWarnings("UnusedAssignment")
    public static void mailMsg(boolean bUI, String cTo, final String cCc, final String cBcc, final String cSubject, final String cMsg) {
		System.out.println("mailMsg(boolean bUI, String cTo)");
		
		// email must have a to field
		if(cTo != null && !cTo.isEmpty()) {
			if(java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(Desktop.Action.MAIL)) {
				try {

					// Set up to field
					cTo = cTo.replace(';', ',');
					String uriString = "mailto:" + cTo;
					boolean addedExtras = false; // used so we know whether to add an ampersand or ?

					// Set up CC if any
					if(cCc != null && !cCc.isEmpty()) {
						addedExtras = true;
						uriString += "?";
						uriString += "cc=" + cCc.replace(';', ',');
					}

					// Set up BCC field if any
					if(cBcc != null && !cBcc.isEmpty()) {
						if(!addedExtras) {
							addedExtras = true;
							uriString += "?";
						}
						else {
							uriString += "&";
						}
						uriString += "bcc=" + cBcc.replace(';', ',');
					}

					// Set up subject if any
					if(cSubject != null && !cSubject.isEmpty()) {
						if(!addedExtras) {
							addedExtras = true;
							uriString += "?";
						}
						else {
							uriString += "&";
						}
						uriString += "subject=" + cSubject.replace(" ", "%20");
					}

					// Set up message if any
					if(cMsg != null && !cMsg.isEmpty()) {
						if(!addedExtras) {
							uriString += "?";
						}
						else {
							uriString += "&";
						}
						uriString += "body=" + cMsg.replace(" ", "%20");
					}

					final java.net.URI uri = java.net.URI.create(uriString);
					Desktop.getDesktop().mail(uri);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			else {
				alert("Emailing is not supported on your platform.");
			}
		}

	}

	public static void mailMsg(final boolean bUI, final String cTo, final String cCc, final String cBcc, final String cSubject) {
		mailMsg(bUI, cTo, cCc, cBcc, cSubject, null);
	}
	public static void mailMsg(final boolean bUI, final String cTo, final String cCc, final String cBcc) {
		mailMsg(bUI, cTo, cCc, cBcc, null, null);
	}
	public static void mailMsg(final boolean bUI, final String cTo, final String cCc) {
		mailMsg(bUI, cTo, cCc, null, null, null);
	}
	public static void mailMsg(final boolean bUI, final String cTo) {
		mailMsg(bUI, cTo, null, null, null, null);
	}

	// Attempt at handling jSon/object as parameter
	public static void mailMsg(final Object jSon) {
		System.out.println("mailMsg(Object o)");
		System.out.println(jSon.getClass().getSimpleName());
	}

    public JSApp() {

        if(showOutput) {
            System.out.println("JSApp.JSApp()");
        }

		setPlatform();
    }

    public JSApp(final int a) {

        if(showOutput) {
            System.out.println("JSApp.JSApp(a)");
        }
    	count = a;
		setPlatform();
    }
    /**
    public JSApp(SwingGUI g) {
    	System.out.println("JSApp.JSApp(g)");
    	gui = g;
    }  */

    public int getCount() {

        if(showOutput) {
            System.out.println("JSApp.getCount()");
        }

    	return count++; 
    }

    public void resetCount() {

        if(showOutput) {
            System.out.println("JSApp.resetCount()");
        }

    	count = 0; 
    }

    public static String getString(final String section, final String key) {
        String returnMessage = "";
        if(section.equals("EScript")) {
            if(key.equals("IDS_GREATER_THAN")) {
                returnMessage = Messages.getMessage("PdfJavaScriptMessage.IDS_GREATER_THAN");
            }
            else if(key.equals("IDS_GT_AND_LT")) {
                returnMessage = Messages.getMessage("PdfJavaScriptMessage.IDS_GT_AND_LT");
            }
            else if(key.equals("IDS_LESS_THAN")) {
                returnMessage = Messages.getMessage("PdfJavaScriptMessage.IDS_LESS_THAN");
            }
            else if(key.equals("IDS_INVALID_MONTH")) {
                returnMessage = Messages.getMessage("PdfJavaScriptMessage.IDS_INVALID_MONTH");
            }
            else if(key.equals("IDS_INVALID_DATE")) {
                returnMessage = Messages.getMessage("PdfJavaScriptMessage.IDS_INVALID_DATE");
            }
            else if(key.equals("IDS_INVALID_VALUE")) {
                returnMessage = Messages.getMessage("PdfJavaScriptMessage.IDS_INVALID_VALUE");
            }
            else if(key.equals("IDS_AM")) {
                returnMessage = Messages.getMessage("PdfJavaScriptMessage.IDS_AM");
            }
            else if(key.equals("IDS_PM")) {
                returnMessage = Messages.getMessage("PdfJavaScriptMessage.IDS_PM");
            }
            else if(key.equals("IDS_MONTH_INFO")) {
                return "\"\"";
            }
            else if(key.equals("IDS_STARTUP_CONSOLE_MSG")) {
                returnMessage = Messages.getMessage("PdfJavaScriptMessage.IDS_STARTUP_CONSOLE_MSG");
            }

        }
        return '"' +returnMessage+ '"';
    }

}
