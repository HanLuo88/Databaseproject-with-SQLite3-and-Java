package de.hhu.cs.dbs.propra.presentation.rest;



import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import java.util.List;


@Path("/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class Control
{
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;


    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    public String makeJsonString(ResultSet rs, String[] keys) throws SQLException
    {
        ResultSetMetaData rsmd = rs.getMetaData();

        List<String> rows = new ArrayList<>();
        int columnsNumber = rsmd.getColumnCount();
        while (rs.next())
        {
            StringBuilder sbRow = new StringBuilder();
            sbRow.append("\n\t{");
            for (int i = 1; i <= columnsNumber; i++)
            {
                String columnValue = rs.getString(i);
                sbRow.append("\n\t\t");

                String key = keys[i-1];
                sbRow.append("\"" + key + "\":\"" + columnValue + "\"");
                if(i<columnsNumber)
                {
                    sbRow.append(",");
                }
                sbRow.append("\n");
            }
            sbRow.append("\t}");
            rows.add(sbRow.toString());

        }


        StringBuilder sbArray = new StringBuilder();
        sbArray.append("[");
        for(int i=0; i<rows.size(); i++)
        {

            sbArray.append(rows.get(i));
            if(i<rows.size()-1)
            {
                sbArray.append(",");
            }

        }
        sbArray.append("\n]");
        return sbArray.toString();

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Path("fahrzeuge")
    @RolesAllowed({"KUNDE"})
    @POST

    public Response addFahrzeug(
            @FormDataParam("modellid") int modellid,
            @FormDataParam("kennzeichen") String kennzeichen,
            @FormDataParam("hudatum") String hudatum,
            @FormDataParam("erstzulassung") String erstzulassung) throws SQLException
    {
        try (Connection connection = this.dataSource.getConnection())
        {
            String email = securityContext.getUserPrincipal().getName();
            String sql =
                    "INSERT INTO Fahrzeug(ModellID, Kennzeichen, HU_datum, Erstzulassung, email) Values(?,?,?,?,?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, modellid);
            preparedStatement.setObject(2, kennzeichen);
            preparedStatement.setObject(3, hudatum);
            preparedStatement.setObject(4, erstzulassung);
            preparedStatement.setObject(5, email);
            preparedStatement.execute();
            return Response.created(uriInfo.getAbsolutePathBuilder().path(kennzeichen).build()).build();
        }
    }
    //Funktioniert
    //Beispiel:
    // curl --user Kunde1@gmail.de:KUnde1  -X POST "http://localhost:8080/fahrzeuge" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "modellid=1" -F "kennzeichen=Kenn-Anspr-3" -F "hudatum=2011-02" -F "erstzulassung=2010-01-01 12:24:59"
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("fahrzeuge")
    @RolesAllowed({"ANSPRECHPARTNER"})
    @GET
    public Response getFahrzeuge(
            @QueryParam("kennzeichen") String kennzeichen,
            @QueryParam("erstzulassungsdatum") String erstzulassungsdatum) throws SQLException
    {
        boolean ok = true;
        try(Connection connection = dataSource.getConnection())
        {
            String getFahrzeuge =
                    "SELECT ModellID, Kennzeichen, Erstzulassung, HU_Datum " +
                            "FROM Fahrzeug " +
                            "WHERE " +
                            "(Kennzeichen LIKE ?) " +
                            "AND " +
                            "(julianday(?) < julianday(Erstzulassung));";
            PreparedStatement preparedStatementgetfahrzeuge = connection.prepareStatement(getFahrzeuge);
            preparedStatementgetfahrzeuge.setObject(1, kennzeichen);
            preparedStatementgetfahrzeuge.setObject(2, erstzulassungsdatum);
            ok = preparedStatementgetfahrzeuge.execute() && ok;

            ResultSet fahrzeugrset = preparedStatementgetfahrzeuge.executeQuery();
            if(ok)
            {

                String[] keys = new String[]{"ModellID", "Kennzeichen", "Erstzulassung", "HU_Datum"};
                String jString = makeJsonString(fahrzeugrset, keys);
                return Response.status(Response.Status.OK).entity(jString).build();
            }
            else
            {
                String fehler2 = "{\"message\":\"Bad Request\"}";
                return Response.status(Response.Status.BAD_REQUEST).entity(fehler2).build();
            }
        }
    }

    //Funktioniert
    //Beispiel
    //curl --user ansprechpartner1@web.de:KLmuell3r  -X GET "http://localhost:8080/fahrzeuge?kennzeichen=Kenn-Anspr-1&erstzulassungsdatum=2000-01-01%2012%3A24%3A59" -H "accept: application/json;charset=UTF-8"
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("auftraege/{auftragid}/ersatzteile")
    @RolesAllowed({"ANSPRECHPARTNER"})
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response getErsatzteilezuauftrag(@PathParam("auftragid") int auftragid,
                                            @QueryParam("bezeichnung") String bezeichnung) throws SQLException
    {
        boolean ok = true;
        try (Connection connection = this.dataSource.getConnection())
        {
            String auftragidexists =
                    "SELECT COUNT(*) " +
                            "FROM Auftrag " +
                            "WHERE ? = Auftragid;";
            PreparedStatement preparedStatementAuftragExists = connection.prepareStatement(auftragidexists);
            preparedStatementAuftragExists.setObject(1, auftragid);
            ok = preparedStatementAuftragExists.execute() && ok;
            ResultSet auftragexists = preparedStatementAuftragExists.executeQuery();
            if (auftragexists.getLong(1) == 0)
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            //Wenn AuftragID existiert:
            String getErsatzteilNr =
                    "SELECT ErsatzteilNr " +
                            "FROM benoetigt " +
                            "WHERE " +
                            "? = AuftragID;";
            PreparedStatement preparedStatementgetErsatzteilNr = connection.prepareStatement(getErsatzteilNr);
            preparedStatementgetErsatzteilNr.setObject(1, auftragid);
            ResultSet ersatzteilnummer = preparedStatementgetErsatzteilNr.executeQuery();
            int ersatzID = ersatzteilnummer.getInt(1);

            ok = preparedStatementgetErsatzteilNr.execute() && ok;

            String getErsatzteil =
                    "SELECT * " +
                            "FROM Ersatzteile " +
                            "WHERE ErsatzteilNr = ? AND Kategoriebezeichnung = ?;";
            PreparedStatement preparedStatementgetErsatzteil = connection.prepareStatement(getErsatzteil);
            preparedStatementgetErsatzteil.setObject(1, ersatzID);
            preparedStatementgetErsatzteil.setObject(2, bezeichnung);
            ok = preparedStatementgetErsatzteil.execute() && ok;
            ResultSet ersatzteil = preparedStatementgetErsatzteil.executeQuery();

            if(ok)
            {
                String[] keys = new String[]{"ErsatzteilNr", "Herstellerinfo", "Ersatz_Bezeichnung", "Bild", "KategorieBezeichnung"};
                String jString = makeJsonString(ersatzteil, keys);

                return Response.status(Response.Status.OK).entity(jString).build();
            }
            else
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
    }

    //Beispiel:
    //Mit Bild: curl --output - --user ansprechpartner1@web.de:KLmuell3r -X GET "http://localhost:8080/auftraege/1/ersatzteile?bezeichnung=Reifen" -H "accept: application/json;charset=UTF-8"
    //Ohne Bild: curl --output - --user ansprechpartner1@web.de:KLmuell3r -X GET "http://localhost:8080/auftraege/2/ersatzteile?bezeichnung=Felgen" -H "accept: application/json;charset=UTF-8"
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("auftraege/{auftragid}/ersatzteile")
    @RolesAllowed({"ANSPRECHPARTNER"})
    @POST
    public Response addAuftragzuErsatzteil(@PathParam("auftragid") String auftragid,
                                           @FormDataParam("ersatzteilid") int ersatzteilid) throws SQLException
    {
        long rowid = 0;
        try(Connection connection = this.dataSource.getConnection())
        {
            String sql =
                    "SELECT COUNT(*) " +
                            "FROM Auftrag " +
                            "WHERE ? = AuftragID;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, auftragid);
            ResultSet rset = preparedStatement.executeQuery();
            if(rset.getLong(1) == 0)
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }


            String sql1 =
                    "INSERT INTO benoetigt(ErsatzteilNr, AuftragID) " +
                            "VALUES(?,?);";
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setObject(1, ersatzteilid);
            preparedStatement1.setObject(2, auftragid);
            preparedStatement1.execute();
            PreparedStatement getIDr = connection.prepareStatement("SELECT last_insert_rowid()");
            ResultSet rset1 = getIDr.executeQuery();
            rowid = rset1.getLong(1);
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(Long.toString(rowid)).build()).build();
    }

    //FUNKTIONIERT
    //Beispiel:
    // curl --user ansprechpartner1@web.de:KLmuell3r -X POST "http://localhost:8080/auftraege/3/ersatzteile" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "ersatzteilid=3"
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("auftraege")
    @RolesAllowed({"ANSPRECHPARTNER"})
    @POST
	public Response addAuftrag(@FormDataParam("fahrzeugid") int fahrzeugid,
                               @FormDataParam("beschreibung") String beschreibung,
                               @FormDataParam("aw_anzahl") int aw_anzahl) throws SQLException
    {
        long row = 0;
        try(Connection connection = dataSource.getConnection())
        {
            String sql =
                    "SELECT Kennzeichen " +
                            "FROM Fahrzeug " +
                            "WHERE Fahrzeug.rowid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, fahrzeugid);
            ResultSet rset = preparedStatement.executeQuery();
            String kennz = rset.getString(1);


            ResultSet resultForRows = preparedStatement.executeQuery();

            int rCount = 0;
            while(resultForRows.next())
            {
                rCount = rCount + 1;
            }
            int auftragid = rCount + 1;
            String email = securityContext.getUserPrincipal().getName();


            String sql1 =
                    "INSERT INTO Auftrag(AuftragID,AW_Anzahl, Beschreibung, Email, Kennzeichen)" +
                            "VALUES(?,?,?,?,?);";
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setObject(1, auftragid);
            preparedStatement1.setObject(2, aw_anzahl);
            preparedStatement1.setObject(3, beschreibung);
            preparedStatement1.setObject(4, email);
            preparedStatement1.setObject(5, kennz);

            PreparedStatement getIDr = connection.prepareStatement("SELECT last_insert_rowid()");
            ResultSet rset1 = getIDr.executeQuery();
            row = rset1.getLong(1);
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(Long.toString(row)).build()).build();
    }

    //Beispiel
    //curl --user ansprechpartner1@web.de:KLmuell3r -X POST "http://localhost:8080/auftraege" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "fahrzeugid=1" -F "beschreibung=reifen wechseln" -F "aw_anzahl=10"
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("kategorien")
    @RolesAllowed({"LIEFERANT"})
    @GET
    public Response getKategorie(@QueryParam("bezeichnung") String bezeichnung) throws SQLException
    {
        boolean ok = true;
        try(Connection connection = this.dataSource.getConnection())
        {

            String sql =
                    "SELECT KategorieBezeichnung " +
                            "FROM Kategorie " +
                            "WHERE KategorieBezeichnung = ? ";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, bezeichnung);
            ok = preparedStatement.execute() && ok;
            ResultSet kategorieSet = preparedStatement.executeQuery();


            if(ok)
            {
                String[] keys = new String[]{"KategorieBezeichnung"};
                String jString = makeJsonString(kategorieSet, keys);

                return Response.status(Response.Status.OK).entity(jString).build();
            }
            else
            {
                String fehler2 = "{\"message\":\"Bad Request\"}";
                return Response.status(Response.Status.BAD_REQUEST).entity(fehler2).build();
            }
        }
    }
    //Funktioniert
    //Beispiel
    //curl --user autoklar@yahoo.de:AUT0klar -X GET "http://localhost:8080/kategorien?bezeichnung=Reifen" -H "accept: application/json;charset=UTF-8"
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("kategorien")
    @RolesAllowed({"LIEFERANT"})
    @POST
    public Response addKategorie(@FormDataParam("bezeichnung") String bezeichnung) throws SQLException
    {
        try(Connection connection = dataSource.getConnection())
        {
            String sql =
                    "INSERT INTO Kategorie(KategorieBezeichnung) " +
                            "VALUES(?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, bezeichnung);
            preparedStatement.execute();
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(bezeichnung).build()).build();
    }

    //Funktioniert
    //Beispiel:
    // curl --user autoklar@yahoo.de:AUT0klar  -X POST "http://localhost:8080/kategorien" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "bezeichnung=Windschutzscheibe"
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("ersatzteile/{ersatzteilid}/liefert")
    @RolesAllowed({"LIEFERANT"})
    @POST
    public Response addErsatzteilZuZulieferer(@PathParam("ersatzteilid") int ersatzteilid,
                                              @FormDataParam("stueckpreis") double stueckpreis) throws SQLException
    {
        long rowid = 0;
        try(Connection connection = dataSource.getConnection())
        {
            String email = securityContext.getUserPrincipal().getName();
            String sql0 =
                    "SELECT COUNT(*) " +
                            "FROM Ersatzteile " +
                            "WHERE ErsatzteilNr = ?;";
            PreparedStatement preparedStatement0 = connection.prepareStatement(sql0);
            preparedStatement0.setObject(1, ersatzteilid);
            ResultSet rset0 = preparedStatement0.executeQuery();
            if(rset0.getLong(1) == 0)
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }


            String sql =
                    "INSERT INTO stellt_bereit(Email, ErsatzteilNr, Stueckpreis) " +
                            "VALUES(?,?,?);";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, email);
            preparedStatement.setObject(2, ersatzteilid);
            preparedStatement.setObject(3, stueckpreis);
            preparedStatement.execute();
            PreparedStatement getIDr = connection.prepareStatement("SELECT last_insert_rowid()");
            ResultSet rset1 = getIDr.executeQuery();
            rowid = rset1.getLong(1);
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(Long.toString(rowid)).build()).build();
    }

    //Beispiel:
    // curl --user autoklar@yahoo.de:AUT0klar -X POST "http://localhost:8080/ersatzteile/4/liefert" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "stueckpreis=999.99"
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("ersatzteile")
    @RolesAllowed({"LIEFERANT"})
    @POST
    public Response addErsatzteil(@FormDataParam("kategorieid") int kategorieid,
                                  @FormDataParam("bezeichnung") String bezeichnung,
                                  @FormDataParam("herstellerinformation") String herstellerinformation,
                                  @FormDataParam("ersatzteilnummer") int ersatzteilnummer,
                                  @FormDataParam("abbildung") String abbildung) throws SQLException
    {

        try(Connection connection = dataSource.getConnection())
        {
            String sql =
                    "SELECT KategorieBezeichnung " +
                            "FROM Kategorie " +
                            "WHERE Kategorie.rowid = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, kategorieid);
            ResultSet rset = preparedStatement.executeQuery();

            String kategorie = rset.getString(1);



            String sql1 =
                    "INSERT INTO Ersatzteile(ErsatzteilNr, Herstellerinfo, Ersatz_Bezeichnung, Bild, KategorieBezeichnung) VALUES(?,?,?,?,?);";
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setObject(1, ersatzteilnummer);
            preparedStatement1.setObject(2, herstellerinformation);
            preparedStatement1.setObject(3, bezeichnung);
            preparedStatement1.setObject(4, abbildung);
            preparedStatement1.setObject(5, kategorie);
            preparedStatement1.execute();
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(Long.toString(ersatzteilnummer)).build()).build();
    }

    //Beispiel:
    //curl --user autoklar@yahoo.de:AUT0klar -X POST "http://localhost:8080/ersatzteile" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "kategorieid=1" -F "bezeichnung=testbezeichnung1" -F "herstellerinformation=bestfit" -F "ersatzteilnummer=5"
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("modelle")
    @GET
    public Response getModell(@QueryParam("bezeichung") String bezeichnung,
                              @QueryParam("hersteller") String hersteller,
                              @QueryParam("kw_staerke") int kw_staerke) throws SQLException
    {
        boolean ok = true;
        try(Connection connection = this.dataSource.getConnection())
        {

            String sql =
                    "SELECT * " +
                            "FROM " +
                            "(SELECT Modell_Bezeichnung, Hersteller, Leistung_in_KW " +
                            "FROM Modell) " +
                            "WHERE 1=1 ";
            if(bezeichnung != null) sql += "AND Modell_Bezeichnung LIKE \'%" + bezeichnung + "%\'";
            if(hersteller != null) sql += "AND Hersteller LIKE \'%" + hersteller + "%\'";
            if(kw_staerke > 0) sql += "AND Leistung_in_KW LIKE \'%" + kw_staerke + "%\'";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            ok = preparedStatement.execute() && ok;
            ResultSet modellrset = preparedStatement.executeQuery();

            if(ok)
            {
                String[] keys = new String[]{"ModellID", "Hersteller", "Modell_Bezeichnung", "Leistung_in_KW", "Typ", "Klasse"};
                String jString = makeJsonString(modellrset, keys);

                return Response.status(Response.Status.OK).entity(jString).build();
            }
            else
            {
                String fehler2 = "{\"message\":\"Bad Request\"}";
                return Response.status(Response.Status.BAD_REQUEST).entity(fehler2).build();
            }
        }
    }

    //Funktioniert
    //Beispiel:
    //curl -X GET "http://localhost:8080/modelle?bezeichnung=3er&hersteller=BMW&kw_staerke=215" -H "accept: application/json;charset=UTF-8"
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("adressen")
    @GET
    public Response getAdressen(@QueryParam("plz") String plz) throws SQLException
    {
        boolean ok = true;
        try(Connection connection = this.dataSource.getConnection())
        {
            String plzexists =
                    "SELECT COUNT(*) " +
                            "FROM Adresse " +
                            "WHERE PLZ = ?;";
            PreparedStatement preparedStatementplzexists = connection.prepareStatement(plzexists);
            preparedStatementplzexists.setObject(1, plz);
            ok = preparedStatementplzexists.execute() && ok;
            ResultSet adressrset = preparedStatementplzexists.executeQuery();
            if (adressrset.getLong(1) == 0)
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String adressen =
                    "SELECT PLZ, Stadt, Strasse, Hausnummer " +
                            "FROM Adresse " +
                            "WHERE PLZ = ?;";
            PreparedStatement preparedStatementAdressen = connection.prepareStatement(adressen);
            preparedStatementAdressen.setObject(1, plz);
            ok = preparedStatementAdressen.execute() && ok;
            ResultSet adresstable = preparedStatementAdressen.executeQuery();

            if (ok)
            {
                String[] keys = new String[]{"PLZ", "Stadt", "Strasse", "Hausnummer"};
                String jString = makeJsonString(adresstable, keys);

                return Response.status(Response.Status.OK).entity(jString).build();
            } else
            {
                String fehler2 = "{\"message\":\"Bad Request\"}";
                return Response.status(Response.Status.BAD_REQUEST).entity(fehler2).build();
            }
        }
    }

    //Funktioniert
    //curl -X GET "http://localhost:8080/adressen?plz=40210" -H "accept: application/json;charset=UTF-8"
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("ersatzteile")
    @GET
    public Response getErsatzteile(@QueryParam("bezeichnung") String bezeichnung,
                                   @QueryParam("ersatzteilnummer") int ersatzteilnummer) throws SQLException
    {
        boolean ok = true;
        try(Connection connection = this.dataSource.getConnection())
        {
            String getErsatzteileAllUser =
                    "SELECT Ersatz_Bezeichnung, ErsatzteilNr " +
                            "FROM Ersatzteile " +
                            "WHERE ? = Ersatz_Bezeichnung " +
                            "AND" +
                            "? = ErsatzteilNr;";
            PreparedStatement preparedStatementAllUserGetErsatzteile = connection.prepareStatement(getErsatzteileAllUser);
            preparedStatementAllUserGetErsatzteile.setObject(1, bezeichnung);
            preparedStatementAllUserGetErsatzteile.setObject(2, ersatzteilnummer);
            ok = preparedStatementAllUserGetErsatzteile.execute() && ok;
            ResultSet rsetgetersatzteileAllUser = preparedStatementAllUserGetErsatzteile.executeQuery();

            if (ok)
            {
                String[] keys = new String[]{"Ersatz_Bezeichnung", "ErsatzteilNr"};
                String jString = makeJsonString(rsetgetersatzteileAllUser, keys);

                return Response.status(Response.Status.OK).entity(jString).build();
            }
            else
            {
                String fehler2 = "{\"message\":\"Bad Request\"}";
                return Response.status(Response.Status.BAD_REQUEST).entity(fehler2).build();
            }
        }
    }
    //Funktioniert
    //Beispiel:
    //curl -X GET "http://localhost:8080/ersatzteile?bezeichnung=DunlopWinterreifen%202000X%20TURBO&ersatzteilnummer=1" -H "accept: application/json;charset=UTF-8"
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("werkstaetten")
    @GET
    public Response getWerkstaetten(@QueryParam("name") String name,
                                    @QueryParam("stadt") String stadt,
                                    @QueryParam("preis") double preis) throws SQLException
    {
        boolean ok = true;
        try(Connection connection = this.dataSource.getConnection())
        {
            String getWerkstattID =
                    "SELECT WerkstattID FROM ansaessig_bei WHERE ansaessig_bei.AdressID = (SELECT AdressID FROM Adresse WHERE  Stadt = ?);";
            PreparedStatement getIDfromWerkstatt = connection.prepareStatement(getWerkstattID);
            getIDfromWerkstatt.setObject(1, stadt);
            ok = getIDfromWerkstatt.execute() && ok;
            ResultSet werkstattIDtable = getIDfromWerkstatt.executeQuery();
            int querywerkstattID = werkstattIDtable.getInt(1);

            String getWerkstaette =
                    "SELECT * FROM Werkstatt WHERE WerkstattID = ? AND Preis_pro_AW <= ? AND WerkstattName = ?;";
            PreparedStatement werkstaette = connection.prepareStatement(getWerkstaette);
            werkstaette.setObject(1, querywerkstattID);
            werkstaette.setObject(2, preis);
            werkstaette.setObject(3, name);
            ok = werkstaette.execute() && ok;
            ResultSet werkstattTable = werkstaette.executeQuery();
            if (ok)
            {
                String[] keys = new String[]{"WerkstattID", "Preis_pro_AW", "WerkstattName"};
                String jString = makeJsonString(werkstattTable, keys);

                return Response.status(Response.Status.OK).entity(jString).build();
            }
            else
            {
                String fehler2 = "{\"message\":\"Bad Request\"}";
                return Response.status(Response.Status.BAD_REQUEST).entity(fehler2).build();
            }
        }
    }
    //Funktioniert
    //Beispiel:
    //curl -X GET "http://localhost:8080/werkstaetten?name=CarGawd&stadt=Koeln&preis=20.0" -H "accept: application/json;charset=UTF-8"
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("ansprechpartner")
    @POST
    public Response addAnsprechpartner(@FormDataParam("adresseid") int adresseid,
                                       @FormDataParam("email") String email,
                                       @FormDataParam("passwort") String passwort,
                                       @FormDataParam("vorname") String vorname,
                                       @FormDataParam("nachname") String nachname,
                                       @FormDataParam("mitarbeiternummer") String mitarbeiternummer,
                                       @FormDataParam("name") String name,
                                       @FormDataParam("preis_je_aw") double preis_je_aw) throws SQLException
    {

        try(Connection connection = dataSource.getConnection())
        {

            String sql =
                    "INSERT INTO Benutzer(Email, Vorname, Nachname, Passwort) " +
                            "VALUES(?,?,?,?);";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, email);
            preparedStatement.setObject(2, vorname);
            preparedStatement.setObject(3, nachname);
            preparedStatement.setObject(4, passwort);
            //preparedStatement.execute();
            preparedStatement.executeUpdate();


            String getWerkstattID =
                    "SELECT WerkstattID " +
                            "FROM ansaessig_bei " +
                            "WHERE AdressID = ?;";
            PreparedStatement preparedStatementID = connection.prepareStatement(getWerkstattID);
            preparedStatementID.setObject(1, adresseid);
            //preparedStatement.execute();
            ResultSet rset = preparedStatementID.executeQuery();

            int werkstattid = rset.getInt(1);



            String sql1 =
                    "INSERT INTO Werkstatt(WerkstattID, Preis_pro_AW, Werkstattname) " +
                            "VALUES(?,?,?);";
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setObject(1, werkstattid);
            preparedStatement1.setObject(2, preis_je_aw);
            preparedStatement1.setObject(3, name);

            String sql2 =
                    "INSERT INTO Ansprechpartner(Email, MA_Nr, WerkstattID) " +
                            "VALUES(?,?,?);";
            PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);
            preparedStatement2.setObject(1, email);
            preparedStatement2.setObject(2, mitarbeiternummer);
            preparedStatement2.setObject(3, werkstattid);
        }

        return Response.created(uriInfo.getAbsolutePathBuilder().path(email).build()).build();
    }

    //Funktioniert
    //Beispiel:
    //curl -X POST "http://localhost:8080/ansprechpartner" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "email=ansprtester@an.de" -F "passwort=AnSprecht3st" -F "vorname=Ansprech" -F "nachname=Testeins" -F "mitarbeiternummer=3" -F "name=CarGawd" -F "preis_je_aw=15.10" -F "adresseid=1"
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("lieferanten")
    @POST
    public Response addLieferant(@FormDataParam("email") String email,
                                 @FormDataParam("passwort") String passwort,
                                 @FormDataParam("vorname") String vorname,
                                 @FormDataParam("nachname") String nachname,
                                 @FormDataParam("name") String name) throws SQLException
    {

        try(Connection connection = dataSource.getConnection())
        {

            String sql =
                    "INSERT INTO Benutzer(Email, Vorname, Nachname, Passwort)" +
                            "VALUES(?,?,?,?);";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, email);
            preparedStatement.setObject(2, vorname);
            preparedStatement.setObject(3, nachname);
            preparedStatement.setObject(4, passwort);

            preparedStatement.executeUpdate();

            String sql1 =
                    "INSERT INTO Lieferant(Email, Haendlername)" +
                            "VALUES(?,?);";
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setObject(1, email);
            preparedStatement1.setObject(2, name);

        }

        return Response.status(Response.Status.CREATED).entity(email).build();

    }

    //Funktioniert
    //curl -X POST "http://localhost:8080/lieferanten" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "email=liefertester1@lief.de" -F "passwort=LieFer1" -F "vorname=Liefer" -F "nachname=Ranteins" -F "name=Super Mechanic"
    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Path("kunden")
    @POST
    public Response addKunde(@FormDataParam("adressid") int adressid,
                             @FormDataParam("email") String email,
                             @FormDataParam("passwort") String passwort,
                             @FormDataParam("vorname") String vorname,
                             @FormDataParam("nachname") String nachname,
                             @FormDataParam("werbeeinwilligung") boolean werbeeinwilligung,
                             @FormDataParam("telefonnummer") String telefonnummer) throws SQLException
    {

        try(Connection connection = dataSource.getConnection())
        {
            String sql =
                    "INSERT INTO Benutzer(Email, Vorname, Nachname, Passwort)" +
                            "VALUES(?,?,?,?);";

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setObject(1, email);
            preparedStatement.setObject(2, vorname);
            preparedStatement.setObject(3, nachname);
            preparedStatement.setObject(4, passwort);
            preparedStatement.executeUpdate();

            String sql1 =
                    "INSERT INTO Kunde(Email, Rueckrufnummer, Werbeeinwilligung, AdressID)" +
                            "VALUES(?,?,?,?);";
            PreparedStatement preparedStatement1 = connection.prepareStatement(sql1);
            preparedStatement1.setObject(1, email);
            preparedStatement1.setObject(2, telefonnummer);
            preparedStatement1.setObject(3, werbeeinwilligung);
            preparedStatement1.setObject(4, adressid);
        }

        return Response.status(Response.Status.CREATED).entity(email).build();

    }

    //Funktioniert
    //Beispiel
    //curl -X POST "http://localhost:8080/kunden" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "adresseid=1" -F "email=kundetester1@kun.de" -F "passwort=KUndet1" -F "vorname=Kund" -F "nachname=etesteins" -F "werbeeinwilligung=false" -F "telefonnummer=164589"

}

