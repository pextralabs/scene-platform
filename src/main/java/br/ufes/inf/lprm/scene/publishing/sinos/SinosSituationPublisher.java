package br.ufes.inf.lprm.scene.publishing.sinos;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import br.ufes.inf.lprm.scene.publishing.SituationPublisher;
import br.ufes.inf.lprm.scene.publishing.sinos.publisher.SinosActivePublisher;
import br.ufes.inf.lprm.scene.publishing.sinos.publisher.SinosInactivePublisher;
import br.ufes.inf.lprm.scene.publishing.sinos.publisher.SinosPublisher;
import br.ufes.inf.lprm.sinos.common.DisconnectionReason;
import br.ufes.inf.lprm.sinos.publisher.SituationChannel;
import br.ufes.inf.lprm.situation.SituationType;

public class SinosSituationPublisher extends SituationPublisher {

	private SinosPublisher publisher;
	private Class<? extends SituationType> type;
	private String host;
	private int port;
	private long delay;
	private long attempts;
	private long timeout;
	
	public SinosSituationPublisher(Class<? extends SituationType> type, String host, int port, long delay, long tries, long timeout) {
		super(type, host, port, delay, tries, timeout);
		this.type = type;
		this.host = host;
		this.port = port;
		this.delay = 1000*delay;
		this.attempts = tries;
		this.timeout = timeout;
		initChannel();
	}
	
	public void initChannel () {
		publisher = new SinosInactivePublisher();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<SituationEventChannel> future = executor.submit(new Task(this));
		
		try {
			SituationEventChannel channel = future.get(timeout, TimeUnit.SECONDS);
			publisher = new SinosActivePublisher(channel);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			executor.shutdownNow();
		}
	}
	
	@Override
	public void publishActivation(SituationType sit) {
		publisher.publishActivation(sit);
	}

	@Override
	public void publishDeactivation(SituationType sit) {
		publisher.publishDeactivation(sit);
	}

	class Task implements Callable<SituationEventChannel>{

		private SinosSituationPublisher publisher;

		public Task(SinosSituationPublisher publisher) {
			this.publisher = publisher;
		}
		
		@Override
		public SituationEventChannel call() throws Exception{
			for(int i = 0; i < attempts; i++){
				try {
					return new SituationEventChannel(host, port, type, publisher);
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
					try {Thread.sleep(delay);} catch (InterruptedException e1) {}
				}
			}
			throw new Exception("Sinos server is unreachable. Attempts of reaching it: " + attempts);
		}
	}
}

class SituationEventChannel extends SituationChannel {

	private SinosSituationPublisher publisher;
	
	public SituationEventChannel(String host, int port, Class<? extends SituationType> situationType, SinosSituationPublisher publisher) throws RemoteException, NotBoundException {
		super(host, port, situationType);
		this.publisher = publisher;
	}

	@Override
	public void onDisconnection(DisconnectionReason arg0) {
		publisher.initChannel();
		
	}
}