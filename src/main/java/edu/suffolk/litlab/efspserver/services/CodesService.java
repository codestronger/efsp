package edu.suffolk.litlab.efspserver.services;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import edu.suffolk.litlab.efspserver.codes.CaseCategory;
import edu.suffolk.litlab.efspserver.codes.CaseType;
import edu.suffolk.litlab.efspserver.codes.CodeDatabase;
import edu.suffolk.litlab.efspserver.codes.CourtLocationInfo;
import edu.suffolk.litlab.efspserver.codes.CrossReference;
import edu.suffolk.litlab.efspserver.codes.DataFieldRow;
import edu.suffolk.litlab.efspserver.codes.Disclaimer;
import edu.suffolk.litlab.efspserver.codes.DocumentTypeTableRow;
import edu.suffolk.litlab.efspserver.codes.FileType;
import edu.suffolk.litlab.efspserver.codes.FilingCode;
import edu.suffolk.litlab.efspserver.codes.FilingComponent;
import edu.suffolk.litlab.efspserver.codes.NameAndCode;
import edu.suffolk.litlab.efspserver.codes.OptionalServices;
import edu.suffolk.litlab.efspserver.codes.PartyType;
import edu.suffolk.litlab.efspserver.codes.ServiceCodeType;

@Path("/codes/")
@Produces({MediaType.APPLICATION_JSON})
public class CodesService {
  // TODO(qs): is there anything that should be logged in this class?
  // private static Logger log = LoggerFactory.getLogger(CodesService.class); 

  private CodeDatabase cd;
  public CodesService(CodeDatabase cd) {
    this.cd = cd;
  }

  @GET
  @Path("/courts")
  public Response getCourts(@Context HttpHeaders httpHeaders) throws SQLException {
    List<String> courts = cd.getAllLocations();
    return Response.ok(courts).build();
  }
  
  @GET
  @Path("/courts/{court_id}/codes")
  public Response getCourtLocationCodes(@Context HttpHeaders httpHeaders, 
      @PathParam("court_id") String courtId) throws SQLException {
    Optional<CourtLocationInfo> info = cd.getFullLocationInfo(courtId);
    if (info.isEmpty()) {
      return Response.status(404).build();
    }
    
    return Response.ok(info.get()).build();
  }
  
  @GET
  @Path("/courts/{court_id}/categories")
  public Response getCategories(@Context HttpHeaders httpHeaders, 
    @PathParam("court_id") String courtId) throws SQLException {
    if (!cd.getAllLocations().contains(courtId)) {
      return Response.status(404).entity("Court does not exist " + courtId).build();
    }
    List<CaseCategory> categories = cd.getCaseCategoriesFor(courtId);

    return Response.ok(categories).build();
  }

  @GET
  @Path("/courts/{court_id}/case_types")
  public Response getCaseTypes(@Context HttpHeaders httpHeaders, 
    @PathParam("court_id") String courtId,
    @QueryParam("category_id") String categoryId) throws SQLException {

      if (!cd.getAllLocations().contains(courtId)) {
        return Response.status(404).entity("Court does not exist " + courtId).build();
      }
      List<CaseType> caseTypes = cd.getCaseTypesFor(courtId, categoryId);

    return Response.ok(caseTypes).build();
  }
  
  @GET
  @Path("/courts/{court_id}/name_suffixes")
  public Response getNameSuffixes(@Context HttpHeaders httpHeaders,
      @PathParam("court_id") String courtId) throws SQLException {
    if (!cd.getAllLocations().contains(courtId)) {
      return Response.status(404).entity("Court does not exist " + courtId).build();
    }
    
    List<NameAndCode> suffixes = cd.getNameSuffixes(courtId);
    return Response.ok(suffixes).build();
  }

  @GET
  @Path("/courts/{court_id}/case_types/{case_type_id}/case_subtypes")
  public Response getCaseSubtypes(@Context HttpHeaders httpHeaders, 
    @PathParam("court_id") String courtId,
    @PathParam("case_type_id}") String caseTypeId) throws SQLException {

      if (!cd.getAllLocations().contains(courtId)) {
        return Response.status(404).entity("Court does not exist " + courtId).build();
      }
      List<NameAndCode> caseSubtypes = cd.getCaseSubtypesFor(courtId, caseTypeId);

    return Response.ok(caseSubtypes).build();
  }
  
  @GET
  @Path("/courts/{court_id}/service_types")
  public Response getServiceTypes(@Context HttpHeaders httpHeaders,
      @PathParam("court_id") String courtId) throws SQLException {
    if (!cd.getAllLocations().contains(courtId)) {
      return Response.status(404).entity("Court does not exist: " + courtId).build();
    }
    List<ServiceCodeType> types= cd.getServiceTypes(courtId); 
    return Response.ok(types).build();
  }

  @GET
  @Path("/courts/{court_id}/procedures_or_remedies")
  public Response getProcedureOrRemedies(@Context HttpHeaders httpHeaders, 
    @PathParam("court_id") String courtId,
    @QueryParam("category_id") String categoryId) throws SQLException {

      if (!cd.getAllLocations().contains(courtId)) {
        return Response.status(404).entity("Court does not exist " + courtId).build();
      }
      List<NameAndCode> procedureRemedies = cd.getProcedureOrRemedy(courtId, categoryId);

    return Response.ok(procedureRemedies).build();
  }

  @GET
  @Path("/courts/{court_id}/filing_types")
  public Response getFilingTypes(@Context HttpHeaders httpHeaders, 
    @PathParam("court_id") String courtId,
    @QueryParam("category_id") String categoryId,
    @QueryParam("type_id") String typeId,
    @QueryParam("initial") boolean initial) throws SQLException {

      if (!cd.getAllLocations().contains(courtId)) {
        return Response.status(404).entity("Court does not exist " + courtId).build();
      }
      List<FilingCode> filingTypes = cd.getFilingType(courtId, categoryId, typeId, initial);

    return Response.ok(filingTypes).build();
  }  

  @GET
  @Path("/courts/{court_id}/damage_amounts")
  public Response getDamageAmounts(@Context HttpHeaders httpHeaders, 
    @PathParam("court_id") String courtId,
    @QueryParam("category_id") String categoryId) throws SQLException {

      if (!cd.getAllLocations().contains(courtId)) {
        return Response.status(404).entity("Court does not exist " + courtId).build();
      }
      List<NameAndCode> damageAmounts = cd.getDamageAmount(courtId, categoryId);

    return Response.ok(damageAmounts).build();
  }  


  @GET
  @Path("/courts/{court_id}/case_types/{case_type_id}/party_types")
  public Response getPartyTypes(@Context HttpHeaders httpHeaders, 
      @PathParam("court_id") String courtId,
      @PathParam("case_type_id}") String caseTypeId) throws SQLException {
    if (!cd.getAllLocations().contains(courtId)) {
      return Response.status(404).entity("Court does not exist " + courtId).build();
    }
    List<PartyType> partyTypes = cd.getPartyTypeFor(courtId, caseTypeId);

    return Response.ok(partyTypes).build();
  }

  @GET
  @Path("/courts/{court_id}/casetypes/{case_type_id}/cross_references")
  public Response getCrossReferences(@Context HttpHeaders httpHeaders,
      @PathParam("court_id") String courtId,
      @PathParam("case_type_id") String caseTypeId) throws SQLException {
    if (!cd.getAllLocations().contains(courtId)) {
      return Response.status(404).entity("Court does not exist " + courtId).build();
    }
    
    List<CrossReference> refs = cd.getCrossReference(courtId, caseTypeId);
    return Response.ok(refs).build();
  }


  @GET
  @Path("/courts/{court_id}/filing_codes/{filing_code_id}/document_types")
  public Response getDocumentTypes(@Context HttpHeaders httpHeaders, 
      @PathParam("court_id") String courtId,
      @PathParam("filing_code_id}") String filingCodeId) throws SQLException {
    if (!cd.getAllLocations().contains(courtId)) {
      return Response.status(404).entity("Court does not exist " + courtId).build();
    }
    List<DocumentTypeTableRow> documentTypes = cd.getDocumentTypes(courtId, filingCodeId);

    return Response.ok(documentTypes).build();
  }

  @GET
  @Path("/courts/{court_id}/filing_codes/{filing_code_id}/motion_types")
  public Response getMotionTypes(@Context HttpHeaders httpHeaders, 
    @PathParam("court_id") String courtId,
    @PathParam("filing_code_id}") String filingCodeId) throws SQLException {

      if (!cd.getAllLocations().contains(courtId)) {
        return Response.status(404).entity("Court does not exist " + courtId).build();
      }
      List<NameAndCode> motionTypes = cd.getMotionTypes(courtId, filingCodeId);

    return Response.ok(motionTypes).build();
  }

  @GET
  @Path("/courts/{court_id}/allowed_file_types")
  public Response getAllowedFileTypes(@Context HttpHeaders httpHeaders, 
      @PathParam("court_id") String courtId) throws SQLException {
    if (!cd.getAllLocations().contains(courtId)) {
      return Response.status(404).entity("Court does not exist " + courtId).build();
    }
    List<FileType> fileTypes = cd.getAllowedFileTypes(courtId);

    return Response.ok(fileTypes).build();
  }
  
  @GET
  @Path("/courts/{court_id}/filing_statuses")
  public Response getFilingStatuses(@Context HttpHeaders httpHeaders,
      @PathParam("court_id") String courtId) throws SQLException {
    if (!cd.getAllLocations().contains(courtId))  {
      return Response.status(404).entity("Court " + courtId + " does not exist").build();
    }
    List<NameAndCode> statuses = cd.getFilingStatuses(courtId);
    return Response.ok(statuses).build();
  }

  @GET
  @Path("/courts/{court_id}/filing_codes/{filing_code_id}/filing_components")
  public Response getFilingComponents(@Context HttpHeaders httpHeaders, 
      @PathParam("court_id") String courtId,
      @PathParam("filing_code_id}") String filingCodeId) throws SQLException {
    if (!cd.getAllLocations().contains(courtId)) {
      return Response.status(404).entity("Court does not exist " + courtId).build();
    }
    List<FilingComponent> filingComponents = cd.getFilingComponents(courtId, filingCodeId);

    return Response.ok(filingComponents).build();
  }  
  
  @GET
  @Path("/courts/{court_id}/filing_codes/{filing_code_id}/optional_services")
  public Response getOptionalServices(@Context HttpHeaders httpHeaders, 
      @PathParam("court_id") String courtId,
      @PathParam("filing_code_id") String filingCodeId) throws SQLException {
    if (cd.getAllLocations().contains(courtId)) {
      return Response.status(400).entity("Court does not exist " + courtId).build();
    }
    
    List<OptionalServices> optionalServices = cd.getOptionalServices(courtId, filingCodeId);
    return Response.ok(optionalServices).build();
  }
  
  @GET
  @Path("/countries/{country}/states")
  public Response getStates(@Context HttpHeaders httpHeaders, 
      @PathParam("country") String country) throws SQLException {
    List<String> stateCodes = cd.getStateCodes(country);

    return Response.ok(stateCodes).build();
  }  

  @GET
  @Path("/courts/{court_id}/languages")
  public Response getLanguages(@Context HttpHeaders httpHeaders, 
    @PathParam("court_id") String courtId) throws SQLException {

      if (!cd.getAllLocations().contains(courtId)) {
        return Response.status(404).entity("Court does not exist " + courtId).build();
      }
      List<String> languages = cd.getLanguages(courtId);

    return Response.ok(languages).build();
  }

  @GET
  @Path("/courts/{court_id}/datafields/{field_name}")
  public Response getDataField(@Context HttpHeaders httpHeaders, 
    @PathParam("court_id") String courtId,
    @PathParam("field_name") String fieldName) throws SQLException {

      if (!cd.getAllLocations().contains(courtId)) {
        return Response.status(404).entity("Court does not exist " + courtId).build();
      }
      DataFieldRow datafieldrow = cd.getDataField(courtId, fieldName);

    return Response.ok(datafieldrow).build();
  }  

  @GET
  @Path("/courts/{court_id}/disclaimer_requirements")
  public Response getDisclaimerRequirements(@Context HttpHeaders httpHeaders, 
    @PathParam("court_id") String courtId) throws SQLException {

      if (!cd.getAllLocations().contains(courtId)) {
        return Response.status(404).entity("Court does not exist " + courtId).build();
      }
      List<Disclaimer> disclaimers = cd.getDisclaimerRequirements(courtId);

    return Response.ok(disclaimers).build();
  }

}