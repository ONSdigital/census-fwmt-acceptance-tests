package uk.gov.ons.census.fwmt.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.data.cache.GatewayCache;
import uk.gov.ons.census.fwmt.repository.GatewayCacheRepository;

/**
 * This class is bare-bones because it's a simple connector between the rest of the code and the caching implementation
 * Please don't subvert this class by touching the GatewayCacheRepository
 * If we ever change from a database to redis, this class will form the breaking point
 */

@Slf4j
@Service
public class GatewayCacheService {
  public final GatewayCacheRepository repository;


  public GatewayCacheService(GatewayCacheRepository repository) {
    this.repository = repository;
  }

  public GatewayCache getByIdAndTypeAndExists(String caseId, int type, boolean exists) {
    return repository.findByCaseIdAndTypeAndExistsInFwmt(caseId, type, exists); }

  public GatewayCache getByIndividualCaseIdAndTypeAndExists(String indCaseId, int type, boolean exists) {
    return repository.findByIndividualCaseIdAndTypeAndExistsInFwmt(indCaseId, type, exists); }

  public boolean doesEstabUprnExist(String uprn) {
    return repository.existsByEstabUprn(uprn);
  };

  public boolean doesEstabUprnAndTypeExist(String estabUprn, int type) {
    return repository.existsByEstabUprnAndType(estabUprn, type);}

  public String getEstabCaseId(String estabUprn) {
    return repository.findByEstabUprn(estabUprn);
  }

  public String getUprnCaseId(String uprn) {
    return repository.findByUprn(uprn);
  }

  public GatewayCache save(GatewayCache cache) {
    return repository.save(cache);
  }

  public GatewayCache getById(String caseId) {
    return repository.findByCaseId(caseId);
  }
}
