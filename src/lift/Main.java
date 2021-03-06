package lift;
import javax.swing.SwingUtilities;

import lift.driver.LiftDriver;
import lift.residents.ResidentsSimulation;
import lift.server.Server;
import lift.server.exception.ConnectionExitsException;
import lift.server.exception.ServerSleepsExeption;
import lift.view.LiftSimulation;


/**
 * @author Micha�
 *
 */
public class Main
{	
	/**
	 * 
	 */
	public Main()
	{
		
	}
	
	/** The entry main() method */
	public static void main(String[] args)
	{
		final Server server = new Server();	
		server.start();
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					new LiftSimulation(server);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});	   
		
		
		// Start drivera windy
		try
		{
			(new Thread(LiftDriver.getInstance(5, server))).start();
		}
		catch (ConnectionExitsException | ServerSleepsExeption e)
		{		
			e.printStackTrace();
		}
		
		// Start modulu mieszkancow
		try
		{
			(new Thread(new ResidentsSimulation(5, server))).start();
		}
		catch (ConnectionExitsException | ServerSleepsExeption e)
		{		
			e.printStackTrace();
		}
	}
}
