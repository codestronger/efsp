package edu.suffolk.litlab.efspserver.docassemble;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.hubspot.algebra.Result;

import edu.suffolk.litlab.efspserver.FilingDoc;
import edu.suffolk.litlab.efspserver.FilingInformation;
import edu.suffolk.litlab.efspserver.LegalIssuesTaxonomyCodes;
import edu.suffolk.litlab.efspserver.Person;
import edu.suffolk.litlab.efspserver.codes.CaseCategory;
import edu.suffolk.litlab.efspserver.services.ExtractError;
import edu.suffolk.litlab.efspserver.services.InfoCollector;
import edu.suffolk.litlab.efspserver.services.InterviewVariable;
import edu.suffolk.litlab.efspserver.services.JsonExtractException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilingInformationDocassembleJacksonDeserializer
    extends StdDeserializer<FilingInformation> {
  private static Logger log = LoggerFactory.getLogger(
      FilingInformationDocassembleJacksonDeserializer.class);

  private static final long serialVersionUID = 1L;

  private LegalIssuesTaxonomyCodes codes;
  private InfoCollector classCollector;
  
  public FilingInformationDocassembleJacksonDeserializer(Class<FilingInformation> t, 
      LegalIssuesTaxonomyCodes codes, InfoCollector collector) {
    super(t);
    this.codes = codes;
    this.classCollector = collector;
  }

  private Result<List<Person>, ExtractError> collectPeople(JsonNode topObj, 
      String potentialMember, InfoCollector collector) {
    if (!topObj.has(potentialMember)) {
      return Result.ok(List.of());  // Just an empty list: we don't know if it's required
    }
    if (!(topObj.get(potentialMember).isObject()
          && topObj.get(potentialMember).has("elements")
          && topObj.get(potentialMember).get("elements").isArray())) {
      return Result.err(ExtractError.malformedInterview(
          potentialMember + " isn't an List with an elements array")); 
    }
    List<Person> people = new ArrayList<Person>();
    JsonNode peopleElements = topObj.get(potentialMember).get("elements");
    for (int i = 0; i < peopleElements.size(); i++) {
      Result<Person, ExtractError> per = 
          PersonDocassembleJacksonDeserializer.fromNode(peopleElements.get(i), collector);
      if (per.isErr()) {
        ExtractError ex = per.unwrapErrOrElseThrow();
        log.warn("Person exception: " + ex);
        return Result.err(ex);
      }
      people.add(per.unwrapOrElseThrow());
    }
    return Result.ok(people);
  }

  public Result<FilingInformation, ExtractError> fromNode(JsonNode node, InfoCollector collector) {
    if (!node.isObject()) {
      ExtractError err = ExtractError.malformedInterview("interview isn't a json object"); 
      collector.error(err);
      return Result.err(err);
    }
    Result<List<Person>, ExtractError> maybeUsers = collectPeople(node, "users", collector);
    if (maybeUsers.isErr()) {
      return maybeUsers.mapOk((per) -> null);
    }
    List<Person> users = maybeUsers.unwrapOrElseThrow();
    if (users.isEmpty()) {
      // TODO(brycew): does this need to be split up into every single thing that could be in users?
      InterviewVariable varExpected = new InterviewVariable("users", 
          "the side of the matter that current person answering this interview is on",
          "ALPeopleList", List.of());
      collector.addRequired(varExpected);
      return Result.err(ExtractError.missingRequired(varExpected));
    }

    if (node.has("user_preferred_language") 
        && node.get("user_preferred_language").isTextual()) {
      users.get(0).setLanguage(
          node.get("user_preferred_language").asText());
    }
    // TODO(brycew): optional
    Optional<String> userEmail = users.get(0).getContactInfo().getEmail(); 
    if (userEmail.isEmpty() || userEmail.get().isBlank()) { 
      InterviewVariable var = new InterviewVariable(
          "users[0].email", "Email is required for at least one user", "text", List.of());
      collector.addRequired(var);
      if (collector.finished()) {
        return Result.err(ExtractError.missingRequired(var));
      }
    }
      
    log.debug("Got users");
    // CONTINUE(brycew):
    Result<List<Person>, ExtractError> maybeOthers = collectPeople(node, "other_parties", collector);
    if (maybeOthers.isErr()) {
      return maybeOthers.mapOk((ppl) -> null);
    }
    final List<Person> otherParties = maybeOthers.unwrapOrElseThrow(); 
    if (otherParties.isEmpty()) {
      InterviewVariable othersExpected = new InterviewVariable("other_parties", 
          "the side of the matter that current person answering this interview is on",
          "ALPeopleList", List.of());
      collector.addOptional(othersExpected);
    }
    log.debug("Got other parties");
      
    Optional<Boolean> userStartedCase = Optional.empty(); 
    if (node.has("user_started_case") 
        && node.get("user_started_case").isBoolean()) {
      userStartedCase = Optional.of(node.get("user_started_case").asBoolean());
    }
    log.debug("user_started_case: " + userStartedCase.orElse(null));

    FilingInformation entities = new FilingInformation();
    if (userStartedCase.isEmpty()) {
      InterviewVariable var = collector.requestVar("user_started_case", 
          "Whether or the user is the plantiff or petitioner", "boolean", List.of("true", "false"));
      collector.addRequired(var);
      if (collector.finished()) {
        return Result.err(ExtractError.missingRequired(var));
      }
    }
    // TODO(brycew): plaintiff and petitioners are both defined. 
    // Typical role might have the difference, take which is available
    boolean started = userStartedCase.get(); 
    if (started) {
      users.forEach((user) -> {
        user.setRole("Plaintiff");
      });
      entities.setPlaintiffs(users);
      otherParties.forEach((other) -> {
        other.setRole("Defendant");
      });
      entities.setDefendants(otherParties);
    } else {
      users.forEach((user) -> {
        user.setRole("Defendant");
      });
      entities.setPlaintiffs(otherParties);
      otherParties.forEach((other) -> {
        other.setRole("Plaintiff");
      });
      entities.setDefendants(users);
    }
      
    // Get the interview metadablock TODO(brycew): just one for now
    JsonNode metadataElems = 
        node.get("interview_metadata").get("elements");
    log.info("Keyset: " + metadataElems.fieldNames());
    if (metadataElems.size() == 1) {
      String key = metadataElems.fieldNames().next();
      JsonNode metadata = metadataElems.get(key).get("elements");
      if (metadata.has("categories") && metadata.get("categories").isArray()) {
        List<String> categories = new ArrayList<String>(); 
        metadata.get("categories").forEach((cat) -> categories.add(cat.asText()));
        log.info("Categories: " + categories.toString());
        Set<String> ecfs = codes.allEcfCaseTypes(categories);
        log.info("ECFs:" + ecfs.size());
        if (ecfs.size() == 1) {
          String caseType = ecfs.iterator().next();
          // TODO(brycew): better title
          CaseCategory caseCat = new CaseCategory(categories.toString(), caseType); 
          entities.setCaseCategory(caseCat);
        }
      }
      if (metadata.has("title") && metadata.get("title").isTextual()) {
        entities.setCaseType(metadata.get("title").asText());
        if (true /* TODO(brycew): fill in subtype? */) {
          InterviewVariable var = collector.requestVar("case_subtype", "TODO(brycew)", "text");
          collector.addOptional(var);
          entities.setCaseSubtype("");
        }
      }
    }
      
    List<String> filingPartyIds = users.stream()
        .map((per) -> per.getIdString())    
        .collect(Collectors.toList());
    List<FilingDoc> filingDocs = new ArrayList<FilingDoc>();
    final InterviewVariable bundleVar = collector.requestVar("al_court_bundle", 
        "The full court bundle", "ALDocumentBundle");
    if (!node.has("al_court_bundle")) {
      collector.addRequired(bundleVar);
      if (collector.finished()) {
        return Result.err(ExtractError.missingRequired(bundleVar));
      }
    }
    if (!node.get("al_court_bundle").isObject() || !node.get("al_court_bundle").has("elements")
        || !node.get("al_court_bundle").get("elements").isArray()) {
      return Result.err(ExtractError.malformedInterview(
          "al_court_bundle should be a JSON object with a elements array (i.e. a python DAList)"));
    }
    JsonNode elems = node.get("al_court_bundle").get("elements");
    if (elems.isEmpty()) {
      collector.addRequired(bundleVar);
      if (collector.finished()) {
        return Result.err(ExtractError.missingRequired(bundleVar));
      }
    }
    for (int i = 0; i < elems.size(); i++) {
      Result<FilingDoc, ExtractError> fil = FilingDocDocassembleJacksonDeserializer.fromNode(elems.get(i), collector);
      if (fil.isOk()) {
        FilingDoc doc = fil.unwrapOrElseThrow();
        doc.setFilingPartyIds(filingPartyIds);
        filingDocs.add(doc);
      } else {
        ExtractError err = fil.unwrapErrOrElseThrow();
        if (err.getType().equals(ExtractError.Type.MissingRequired)) {
          collector.addRequired(bundleVar); 
          return Result.err(err);
        }
      }
    }
    if (node.has("comments_to_clerk") 
        && node.get("comments_to_clerk").isTextual()) {
      String filingComments = node.get("comments_to_clerk").asText("");
      if (!filingDocs.isEmpty()) {
        filingDocs.get(0).setFilingComments(filingComments);
      }

    }
    entities.setFilings(filingDocs);
      
    return Result.ok(entities);
  }

  @Override
  public FilingInformation deserialize(JsonParser p, DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    JsonNode node = p.readValueAsTree();
    Result<FilingInformation, ExtractError> info = fromNode(node, this.classCollector);
    if (info.isErr()) {
      throw new JsonExtractException(p, info.unwrapErrOrElseThrow());
    }
    return info.unwrapOrElseThrow();
  }

}