package ar.santandertecnologia.transferencias.kitkat.commons.logger.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class LoggerMap extends LinkedHashMap<Thread, LoggerData> {
  @Override
  protected boolean removeEldestEntry(Map.Entry<Thread, LoggerData> eldest) {
    return this.size() > 100;
  }
}
