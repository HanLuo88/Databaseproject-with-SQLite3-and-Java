insert into Kategorie(KategorieBezeichnung) values("Reifen");
insert into Kategorie(KategorieBezeichnung) values("Felgen");


insert into Benutzer(Email, Vorname, Nachname, Passwort) 
values("autoklar@yahoo.de", "auto", "klar", "AUT0klar");
insert into Benutzer(Email, Vorname, Nachname, Passwort) 
values("bestfit@web.de", "best", "fit", "BE5tfit");
insert into Benutzer(Email, Vorname, Nachname, Passwort) 
values("ansprechpartner1@web.de", "Klaus", "Mueller", "KLmuell3r");
insert into Benutzer(Email, Vorname, Nachname, Passwort) 
values("ansprechpartner2@gmx.de", "Heinz", "Schmitt", "HSchm1tt");
insert into Benutzer(Email, Vorname, Nachname, Passwort) 
values("Kunde1@gmail.de", "Kunde", "Eins", "KUnde1");
insert into Benutzer(Email, Vorname, Nachname, Passwort) 
values("Kunde2@tonline.de", "Kundez", "Wei", "KUnde2");



insert into Adresse(AdressID, Stadt, PLZ, Strasse, Hausnummer)
values(1, "Koeln", 50735, "erste Strasse", '1a');
insert into Adresse(AdressID, Stadt, PLZ, Strasse, Hausnummer)
values(2, "Duesseldorf", 40210, "zweite Strasse", '2');
insert into Adresse(AdressID, Stadt, PLZ, Strasse, Hausnummer)
values(3, "Duesseldorf", 40210, "dritte Strasse", '3');
insert into Adresse(AdressID, Stadt, PLZ, Strasse, Hausnummer)
values(4, "Duesseldorf", 40210, "vierte Strasse", '4');


insert into Kunde(Email, Rueckrufnummer, Werbeeinwilligung, AdressID)
values("Kunde1@gmail.de", 0211123456, 0, 3);
insert into Kunde(Email, Rueckrufnummer, Werbeeinwilligung, AdressID)
values("Kunde2@tonline.de", 0211654321, 1, 4);


insert into Werkstatt(WerkstattID, Preis_pro_AW, WerkstattName)
values(1, 15.10, "CarGawd");
insert into Werkstatt(WerkstattID, Preis_pro_AW, WerkstattName)
values(2, 17.31, "Dr. Car");


insert into Lieferant(Email, Haendlername) values("autoklar@yahoo.de", "autoklar");
insert into Lieferant(Email, Haendlername) values("bestfit@web.de", "bestfit");


insert into Ersatzteile(ErsatzteilNr, Herstellerinfo, Ersatz_Bezeichnung, Bild, KategorieBezeichnung)
values(1, "bestfit", "DunlopWinterreifen 2000X TURBO", readfile('dunlop.png'), "Reifen");
insert into Ersatzteile(ErsatzteilNr, Herstellerinfo, Ersatz_Bezeichnung, KategorieBezeichnung)
values(2, "autoklar", "AluSuperDuper 3000", "Felgen");
insert into Ersatzteile(ErsatzteilNr, Herstellerinfo, Ersatz_Bezeichnung, KategorieBezeichnung)
values(3, "bestfit", "Bridgestone 5000", "Reifen");
insert into Ersatzteile(ErsatzteilNr, Herstellerinfo, Ersatz_Bezeichnung, KategorieBezeichnung)
values(4, "autoklar", "Megafelge Turbo X", "Felgen");


insert into stellt_bereit(Email, ErsatzteilNr, Stueckpreis)
values("bestfit@web.de", 1, 69.99);
insert into stellt_bereit(Email, ErsatzteilNr, Stueckpreis)
values("autoklar@yahoo.de", 2, 59.99);


insert into Ansprechpartner(Email, MA_Nr, WerkstattID)
values("ansprechpartner1@web.de", 1, 1);
insert into Ansprechpartner(Email, MA_Nr, WerkstattID)
values("ansprechpartner2@gmx.de", 2, 2);

insert into Modell(ModellID, Hersteller, Modell_Bezeichnung, Leistung_in_KW, Typ, Klasse)
values(1, "BMW", "3er", 215, "Limousine", "PKW");
insert into Modell(ModellID, Hersteller, Modell_Bezeichnung, Leistung_in_KW, Typ, Klasse)
values(2, "Audi", "A6", 210, "Limousine", "PKW");


insert into Fahrzeug(Kennzeichen, Erstzulassung, HU_Datum, Email, ModellID)
values("Kenn-Anspr-1", "2010-01-01 12:24:59", "2010-02", "Kunde1@gmail.de", 1);
insert into Fahrzeug(Kennzeichen, Erstzulassung, HU_Datum, Email, ModellID)
values("Kenn-Anspr-2", "2011-01-01 14:15:24", "2011-05", "Kunde2@tonline.de", 2);


insert into Auftrag(AuftragID, Auftragsdatum, AW_Anzahl, Beschreibung, Email, Kennzeichen)
values(1, datetime('2019-06-15 12:54:23'), 10, "Reifen wechseln", "ansprechpartner1@web.de", "Kenn-Anspr-1");
insert into Auftrag(AuftragID, Auftragsdatum, AW_Anzahl, Beschreibung, Email, Kennzeichen)
values(2, datetime('now'), 5, "Felgen wechseln", "ansprechpartner2@gmx.de", "Kenn-Anspr-2");
insert into Auftrag(AuftragID, Auftragsdatum, AW_Anzahl, Beschreibung, Email, Kennzeichen)
values(3, datetime('now'), 5, "Auftrag 3", "ansprechpartner2@gmx.de", "Kenn-Anspr-2");
insert into Auftrag(AuftragID, Auftragsdatum, AW_Anzahl, Beschreibung, Email, Kennzeichen)
values(4, datetime('2020-06-12 11:34:45'), 5, "Auftrag 4", "ansprechpartner1@web.de", "Kenn-Anspr-1");
insert into Auftrag(AuftragID, Auftragsdatum, AW_Anzahl, Beschreibung, Email, Kennzeichen)
values(5, datetime('2015-05-07 12:54:14'), 5, "Auftrag 5", "ansprechpartner2@gmx.de", "Kenn-Anspr-2");
insert into Auftrag(AuftragID, Auftragsdatum, AW_Anzahl, Beschreibung, Email, Kennzeichen)
values(6, datetime('2000-03-12 15:24:28'), 5, "Auftrag 6", "ansprechpartner2@gmx.de", "Kenn-Anspr-2");



insert into benoetigt(ErsatzteilNr, AuftragID) values(1, 1);
insert into benoetigt(ErsatzteilNr, AuftragID) values(2, 2);
insert into benoetigt(ErsatzteilNr, AuftragID) values(3, 1);
insert into benoetigt(ErsatzteilNr, AuftragID) values(4, 4);
insert into benoetigt(ErsatzteilNr, AuftragID) values(4, 2);
insert into benoetigt(ErsatzteilNr, AuftragID) values(3, 2);
insert into benoetigt(ErsatzteilNr, AuftragID) values(4, 1);
insert into benoetigt(ErsatzteilNr, AuftragID) values(4, 3);
insert into benoetigt(ErsatzteilNr, AuftragID) values(3, 4);
insert into benoetigt(ErsatzteilNr, AuftragID) values(4, 5);
insert into benoetigt(ErsatzteilNr, AuftragID) values(4, 6);
insert into benoetigt(ErsatzteilNr, AuftragID) values(3, 6);
insert into benoetigt(ErsatzteilNr, AuftragID) values(2, 4);
insert into benoetigt(ErsatzteilNr, AuftragID) values(2, 5);
insert into benoetigt(ErsatzteilNr, AuftragID) values(2, 6);
insert into benoetigt(ErsatzteilNr, AuftragID) values(2, 3);



insert into ansaessig_bei(AdressID, WerkstattID) values(1, 1);
insert into ansaessig_bei(AdressID, WerkstattID) values(2, 2);


insert into bewerten(WerkstattID, Email, Schulnote) values(1, "Kunde1@gmail.de", 3);
insert into bewerten(WerkstattID, Email, Schulnote) values(2, "Kunde2@tonline.de", 2);


insert into wird_ersetzt(ErsatzteilNr1, ErsatzteilNr2) values(1, 3);
insert into wird_ersetzt(ErsatzteilNr1, ErsatzteilNr2) values(2, 4);




