/**
 * 
 */
package lift.server;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

import lift.common.events.*;

/**
 * Przetwarza wiadomosci otrzymane od listenerow i wysyla je do odpowiednich klientow
 * 
 * @author Micha� Chilczuk
 *
 */
class Worker implements Runnable
{
	/** Mapa kanalow do ktorych moze wysylac wiadomosci */
	private final ConcurrentHashMap<ModuleID, Channel<LiftEvent>> channels;
	/** Mapa strategii dla danych modulow */
	private final ConcurrentHashMap<ModuleID, ClientStrategy> clientsStrategies;
	/** Kanal w ktorym przesylane sa wiadomosci do przetworzenia */
	private final Channel<Packet> recieved;
	
	private final Object monitor;
	
	public Worker(final Channel<Packet> recieved)
	{
		this.channels = new ConcurrentHashMap<>();
		this.clientsStrategies = new ConcurrentHashMap<>();
		this.recieved = recieved;
		this.monitor = new Object();
		
		ClientStrategy liftStrategy = new ClientStrategy();
		liftStrategy.addStrategy(ChangeDirectionEvent.class, new ChangeDirectionStrategy());
		liftStrategy.addStrategy(LiftIsReadyEvent.class, new LiftIsReadyStrategy());
		liftStrategy.addStrategy(LiftOnTheFloorEvent.class, new LiftOnTheFloorStrategy());
		clientsStrategies.put(ModuleID.WINDA, liftStrategy);
		
		ClientStrategy symStrategy = new ClientStrategy();
		symStrategy.addStrategy(GetOffEvent.class, new GetOffStrategy());
		symStrategy.addStrategy(GetOnEvent.class, new GetOnStrategy());
		symStrategy.addStrategy(DownButtonEvent.class, new DownButtonStrategy());
		symStrategy.addStrategy(UpButtonEvent.class, new UpButtonStrategy());
		symStrategy.addStrategy(GeneratePersonEvent.class, new GeneratePersonStrategy());
		clientsStrategies.put(ModuleID.MIESZKANCY, symStrategy);
		
		ClientStrategy guiStrategy = new ClientStrategy();
		guiStrategy.addStrategy(SimulationStopEvent.class, new SimulationStopStrategy());
		guiStrategy.addStrategy(SimulationStartEvent.class, new SimulationStartStrategy());
		guiStrategy.addStrategy(StepSimulationEvent.class, new StepSimulationStrategy());
		clientsStrategies.put(ModuleID.GUI, guiStrategy);
		
		System.out.println(clientsStrategies.size());
	}
	
	/**
	 * Dodaje nowy kanal do ktorego moze wysylac wiadomosci.
	 *  
	 * @param id identyfikator klienta
	 * @param channel kanal do ktorego bedzie wysylal
	 */
	public void addChannel(final ModuleID id, final Channel<LiftEvent> channel)
	{
		channels.putIfAbsent(id, channel);
	}
	
	/**
	 * Usuwa kanal klienta o danym id
	 * 
	 * @param id identyfikator klienta usuwanego kanalu
	 */
	public void removeChannel(final ModuleID id)
	{
		channels.remove(id);
	}

	@Override
	public void run()
	{
		while(true)
		{
			synchronized (monitor)
			{
				try
				{
					monitor.wait();
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			JOptionPane.showMessageDialog(null, "mon");
			Packet packet = recieved.get();
			
			ModuleID sender = packet.getSender();
			LiftEvent event = packet.getEvent();
			
			clientsStrategies.get(sender).process(event);
		}
	}
	
	/**
	 * Strategia obslugi klienta
	 * 
	 * @author Micha� Chilczuk
	 *
	 */
	class ClientStrategy
	{
		/** strategie dla danego klienta */
		private final HashMap<Class<? extends LiftEvent>, LiftEventStrategy> strategies;
		
		public ClientStrategy()
		{
			this.strategies = new HashMap<>();
		}
		
		/** 
		 * Wykonuje dana strategie 
		 * 
		 * @param event wiadomosc dla ktorej ma byc wykonana strategia
		 * 
		 */
		public void process(final LiftEvent event)
		{
			LiftEventStrategy strategy = this.strategies.get(event.getClass());
			if(strategy != null)
			{
				strategy.execute(event);
			}
		}
		
		/**
		 * dodaje strategie dla danej wiadomosci
		 * 
		 * @param event wiadomosc dla ktorej bedzie wykonywana strategia
		 * @param strategy strtegia do wykonania dla danej wiadomosci
		 */
		public void addStrategy(final Class<? extends LiftEvent> event, final LiftEventStrategy strategy)
		{
			System.out.println("dodaje strategie dla: " + event);
			this.strategies.put(event, strategy);
		}
		
		/**
		 * Usuwa strategie dla danej wiadomosci
		 * 
		 * @param event wiadomosc dla ktorej bedzie usunieta strategia
		 * 
		 */
		public void removeStrategy(final Class<? extends LiftEvent> event)
		{
			this.strategies.remove(event);
		}
	}
	
	/**
	 * Strategia obslugi eventu
	 * 
	 * @author Micha� Chilczuk
	 *
	 */
	abstract class LiftEventStrategy
	{
		public abstract void execute(final LiftEvent event);
	}
	
	class GetOffStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.WINDA).add(event);
		}
	}
	
	class GetOnStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.WINDA).add(event);
		}
	}
	
	class DownButtonStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.WINDA).add(event);
		}
	}
	
	class UpButtonStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.WINDA).add(event);
		}
	}
	
	class GeneratePersonStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.WINDA).add(event);
		}
	}
	
	class ChangeDirectionStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.MIESZKANCY).add(event);
		}
	}
	
	class LiftIsReadyStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.MIESZKANCY).add(event);
		}
	}
	
	class LiftOnTheFloorStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.MIESZKANCY).add(event);
		}
	}
	
	class SimulationStopStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.MIESZKANCY).add(event);
			channels.get(ModuleID.WINDA).add(event);
		}
	}
	
	class SimulationStartStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.MIESZKANCY).add(event);
			channels.get(ModuleID.WINDA).add(event);
		}
	}
	
	class StepSimulationStrategy extends LiftEventStrategy
	{
		@Override
		public void execute(final LiftEvent event)
		{
			channels.get(ModuleID.MIESZKANCY).add(new SimulationStopEvent());
			channels.get(ModuleID.WINDA).add(new SimulationStopEvent());
			
			synchronized (monitor)
			{
				monitor.notify();
			}
		}
	}
}
