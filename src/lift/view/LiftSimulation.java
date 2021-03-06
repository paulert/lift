package lift.view;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lift.common.events.GuiGeneratePersonEvent;
import lift.common.events.SimulationStartEvent;
import lift.common.events.SimulationStopEvent;
import lift.server.Connection;
import lift.server.ModuleID;
import lift.server.Server;
import lift.server.exception.ConnectionExitsException;
import lift.server.exception.ServerSleepsExeption;

public class LiftSimulation extends JFrame {
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
// Name-constants for the various dimensions
   public static final int CANVAS_WIDTH = 640;
   public static final int CANVAS_HEIGHT = 504;
   public static final Color CANVAS_BG_COLOR = new Color(183, 221, 230);
   public static final int IMAGE_WIDTH = 85;
   public static final int IMAGE_HEIGHT = 126;
 
   private DrawCanvas canvas; // the custom drawing canvas (extends JPanel)

   private ArrayList<Man> residentsList;
   private Man man;
   private Man man2;
   private Building building;
   private ElevatorShaft shaft;
   private ElevatorBox box;
   
   /** Polaczenie z serwerem */
   private final Connection connection;

 
   /** 
    * Constructor to set up the GUI
    * 
    * @throws ConnectionExitsException
    * @throws ServerSleepsExeption
    *  
 	*/
   public LiftSimulation(final Server server) throws Exception
   {  
	  // Set up elements of the Simulation
      man = new Man(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 4, 2);
      man2 = new Man(0, IMAGE_HEIGHT, IMAGE_WIDTH, IMAGE_HEIGHT, 1, 3);
      building = new Building(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, Color.BLACK);
      shaft = new ElevatorShaft(CANVAS_WIDTH-IMAGE_WIDTH, 0, IMAGE_WIDTH, CANVAS_HEIGHT, Color.YELLOW);
      box = new ElevatorBox(CANVAS_WIDTH-IMAGE_WIDTH, 0, IMAGE_WIDTH, IMAGE_HEIGHT, Color.DARK_GRAY);
      
      try
      {
		this.connection = server.connect(ModuleID.GUI);
      }
      catch (ConnectionExitsException | ServerSleepsExeption e)
      {
    	  e.printStackTrace();
    	  throw e;
      }
      
      residentsList = new ArrayList<Man>();
      residentsList.add(man);
      residentsList.add(man2);
      
      
      // Set up the custom drawing canvas (JPanel)
      canvas = new DrawCanvas();
      canvas.setPreferredSize(new Dimension(CANVAS_WIDTH+10, CANVAS_HEIGHT+10));
      
 
      // Set up a panels for the buttons
      JPanel btnPanel = new JPanel(new FlowLayout());
      JPanel addResidentPanel = new JPanel(new FlowLayout());

      // Set up buttons
      JButton startSimulation = new JButton("Start simulation");
      JButton stopSimulation = new JButton("Stop simulation");
      JButton addNewResident = new JButton("Add new Resident ");
      JButton btnMoveManLeft = new JButton("Move Man Left ");
      JButton btnMoveManRight= new JButton("Move Man Right ");
      JButton btnMoveBoxUp = new JButton("Move Box Up");
      JButton btnMoveBoxDown= new JButton("Move Box Down");
      
      // Set up TextFields and Labels
      final JLabel srcFloorJLabel = new JLabel("Source Floor:");
      final JTextField srcJTextField = new JTextField(2);
      final JLabel dstFloorJLabel = new JLabel("Destination Floor:");
      final JTextField dstJTextField = new JTextField(2);
      
      // Add buttons to Panels
      addResidentPanel.add(startSimulation);
      addResidentPanel.add(stopSimulation);
      addResidentPanel.add(addNewResident);
      addResidentPanel.add(srcFloorJLabel);
      addResidentPanel.add(srcJTextField);
      addResidentPanel.add(dstFloorJLabel);
      addResidentPanel.add(dstJTextField);
      
      btnPanel.add(btnMoveManLeft);
      btnPanel.add(btnMoveManRight);
      btnPanel.add(btnMoveBoxUp);
      btnPanel.add(btnMoveBoxDown);
      
      startSimulation.addActionListener(new ActionListener()
      {    		
    		@Override
    		public void actionPerformed(ActionEvent arg0)
    		{
    			connection.send(new SimulationStartEvent());    			
    		}
      });
      
      stopSimulation.addActionListener(new ActionListener()
      {    		
    		@Override
    		public void actionPerformed(ActionEvent arg0)
    		{
    			connection.send(new SimulationStopEvent());    			
    		}
      });
      
      // add Listeners to buttons
      addNewResident.addActionListener(new ActionListener() {
  		
    	  @Override
  		public void actionPerformed(ActionEvent arg0)
  		{
    		int homeFloor = Integer.parseInt(srcJTextField.getText());
    		int destinationFloor = Integer.parseInt(dstJTextField.getText());
    		
  			connection.send(new GuiGeneratePersonEvent(homeFloor, destinationFloor));    			
  		}
  	});
      
      btnMoveManLeft.addActionListener(new ActionListener()
      {		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			moveManLeft();
			openTheDoor();
            requestFocus(); // change the focus to JFrame to receive KeyEvent
			
		}
	});
      btnMoveManRight.addActionListener(new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			moveManRight();
			manWalkIntoLift();
			closeTheDoor();
            requestFocus(); // change the focus to JFrame to receive KeyEvent
			
		}
	});
      
      btnMoveBoxUp.addActionListener(new ActionListener() {
  		
  		@Override
  		public void actionPerformed(ActionEvent arg0) {
  			// TODO Auto-generated method stub
  			moveBoxUp();
              requestFocus(); // change the focus to JFrame to receive KeyEvent
  			
  		}
  	});
        btnMoveBoxDown.addActionListener(new ActionListener() {
  		
  		@Override
  		public void actionPerformed(ActionEvent arg0) {
  			// TODO Auto-generated method stub
  			moveBoxDown();
              requestFocus(); // change the focus to JFrame to receive KeyEvent
  			
  		}
  	});
 
      // Add both panels to this JFrame
      Container cp = getContentPane();
      cp.setLayout(new BorderLayout());
      cp.add(canvas, BorderLayout.CENTER);
      cp.add(btnPanel, BorderLayout.SOUTH);
      cp.add(addResidentPanel, BorderLayout.NORTH);

 
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setTitle("Real Time Lift Simulator");
      pack();            // pack all the components in the JFrame
      setVisible(true);  // show it
      requestFocus();    // "this" JFrame requests focus to receive KeyEvent
   }
   
   /**
    * Startuje modul GUI
    * 
    */
   public void start()
   {
	   
   }

   public void openTheDoor() 
   {
	   Thread animationThread = new Thread () {
         @Override
	         public void run() {
			   while(box.width > 0)
			   {
				   box.width--;
				   canvas.repaint();
				   try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			   
		       }
         	}
	   };
	   animationThread.start(); 
   }
   
   public void closeTheDoor() 
   {
	   Thread animationThread = new Thread () {
         @Override
	         public void run() {
			   while(box.width < IMAGE_WIDTH)
			   {
				   box.width++;
				   canvas.repaint();
				   try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			   
		       }
         	}
	   };
	   animationThread.start(); 
   }
   
   public void manWalkIntoLift()
   {
	   Thread animationThread = new Thread () {
	         @Override
		         public void run() {
				   while(man.x < CANVAS_WIDTH-IMAGE_WIDTH)
				   {
					   man.x += 8;
					   canvas.repaint();
					   try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				   
			       }
	         	}
		   };
		   animationThread.start(); 
   }

/** Helper method to move the sprite left */
   private void moveManLeft() {
      // Save the current dimensions for repaint to erase the sprite
      int savedX = man.x;
      // update sprite
      man.x -= 10;
      // Repaint only the affected areas, not the entire JFrame, for efficiency
      canvas.repaint(savedX, man.y, man.width, man.height); // Clear old area to background
      canvas.repaint(man.x, man.y, man.width, man.height); // Paint new location
   }
   
   /** Helper method to move the sprite left */
   private void moveManRight() {
      // Save the current dimensions for repaint to erase the sprite
      int savedX = man.x;
      // update sprite
      man.x += 10;
      // Repaint only the affected areas, not the entire JFrame, for efficiency
      canvas.repaint(savedX, man.y, man.width, man.height); // Clear old area to background
      canvas.repaint(man.x, man.y, man.width, man.height); // Paint new location
   }
   
   /** Helper method to move the sprite left */
   private void moveBoxUp() {
      // Save the current dimensions for repaint to erase the sprite
      int saved_y = box.y;
      // update sprite
      box.y -= 5;  // coordinate system in Java is reversed!!
      // Repaint only the affected areas, not the entire JFrame, for efficiency
      canvas.repaint(box.x, saved_y, box.width, box.height); // Clear old area to background
      canvas.repaint(box.x, box.y, box.width, box.height); // Paint new location
   }
   
   /** Helper method to move the sprite left */
   private void moveBoxDown() {
      // Save the current dimensions for repaint to erase the sprite
      int saved_y = box.y;
      // update sprite
      box.y += 5;	 // coordinate system in Java is reversed!!
      // Repaint only the affected areas, not the entire JFrame, for efficiency
      canvas.repaint(box.x, saved_y, box.width, box.height); // Clear old area to background
      canvas.repaint(box.x, box.y, box.width, box.height); // Paint new location
   }
 
 
   /** DrawCanvas (inner class) is a JPanel used for custom drawing */
   class DrawCanvas extends JPanel {
    
	private static final long serialVersionUID = 1L;

	@Override
      public void paintComponent(Graphics g) {
         super.paintComponent(g);
         setBackground(CANVAS_BG_COLOR);

         for (Man person : residentsList) {
			person.paint(g);
         }
         
         building.paint(g);
         shaft.paint(g);
         box.paint(g);
         
      }
   }
}