package dbapp.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class ClosableTabbedPane extends JTabbedPane {
   private ImageIcon tabCloseOnIcon = null;
   private ImageIcon tabCloseOffIcon = null;
   
   public ClosableTabbedPane(ImageIcon onIcon, ImageIcon offIcon) {
      tabCloseOnIcon = onIcon;
      tabCloseOffIcon = offIcon;
   }
   
   public void addTab(String title, Icon icon, Component component, String tip, Action action, boolean isClosable) {
      insertTab(title, icon, component, tip, this.getTabCount(), action, isClosable);
   }
   
   public void addTab(String title, Icon icon, Component component, String tip, Action action) {      
      insertTab(title, icon, component, tip, this.getTabCount(), action, true);
   }
   
   public void insertTab(String title, Icon icon, Component component, String tip, int index, Action action, boolean isClosable) {
      insertTab(title, icon, component, tip, index);
      setTabComponentAt(index, new TabPanel(title, icon, component, action, isClosable));
   }
   
   public boolean isClosable(int index) {
      TabPanel tabComponent = (TabPanel) getTabComponentAt(index);
      return tabComponent.isClosable();
   }
   
   public boolean isClosable(Component component) {
      int index = indexOfComponent(component);
      TabPanel tabComponent = (TabPanel) getTabComponentAt(index);
      return tabComponent.isClosable();
   }
   
   public void setClosable(int index, boolean closable) {
      TabPanel tabComponent = (TabPanel) getTabComponentAt(index);
      tabComponent.setClosable(closable);      
   }
   
   public void setClosable(Component component, boolean closable) {
      int index = indexOfComponent(component);
      TabPanel tabComponent = (TabPanel) getTabComponentAt(index);
      tabComponent.setClosable(closable);
   }   
   
   class TabPanel extends JPanel implements ActionListener {
      Component target;
      ActionListener closeAction = null;
      JButton closeButton = null;
      
      TabPanel(String title, Icon icon, Component component, ActionListener action, boolean closable) {
         target = component;
         closeAction = action;
         
         setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
         add(new JLabel(title, icon, LEFT));
         add(Box.createHorizontalStrut(7));

         closeButton = createPlatCloseButton(component, action, closable);
         add(closeButton);    
      }

      boolean isClosable() {
         return closeButton.isEnabled();
      }
      
      void setClosable(boolean closable) {
         closeButton.setEnabled(closable);
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
         // fire closeAction first.
         if(closeAction != null)
            closeAction.actionPerformed(e);
         
         // now, close the tab.
         int removeIndex = indexOfComponent(target);
         if (target != null && removeIndex != -1)
            removeTabAt(removeIndex);
      }
      
      private JButton createPlatCloseButton(Component component, ActionListener action, boolean closable) {
         JButton b = new JButton(tabCloseOffIcon);
         b.setRolloverIcon(tabCloseOnIcon);
         b.setBorder(null);
         b.setBorderPainted(false);   // no need when border is set to null.
         b.setFocusPainted(false);
         b.setContentAreaFilled(false);

         b.addActionListener(this);
         b.setEnabled(closable);
         
         return b;
      }
   }
}
