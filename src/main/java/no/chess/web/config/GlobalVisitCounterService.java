package no.chess.web.config;

import org.springframework.stereotype.Service;

//Spring Boot vil automatisk gjøre denne til en Singleton
@Service
public class GlobalVisitCounterService {

    public int getGlobalCount() {
		return globalCount;
	}

	public void setGlobalCount(int globalCount) {
		this.globalCount = globalCount;
	}

	// volatile sikrer at variabelen synkroniseres korrekt mellom tråder
    private volatile int globalCount = 0;

    /**
     * Øker den globale telleren på en trådsikker måte.
     * @return Den nye globale tellingen.
     */
    public synchronized int incrementAndGetCount() {
        globalCount++;
        return globalCount;
    }
}
