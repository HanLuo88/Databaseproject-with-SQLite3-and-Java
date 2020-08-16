PRAGMA auto_vacuum = 1;
PRAGMA encoding = "UTF-8";
PRAGMA foreign_keys = 1;
PRAGMA journal_mode = WAL;
PRAGMA synchronous = NORMAL;

create table IF NOT EXISTS Kategorie
(
	KategorieBezeichnung			varchar[50] primary key collate nocase check(length(KategorieBezeichnung)>0 and KategorieBezeichnung not glob '*[^a-zA-Z]*')
									
);

create table IF NOT EXISTS Benutzer
(
	Email							varchar[50] primary key collate nocase check(length(Email)>0 and substr(Email, 0, instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email,'@')+1, instr(Email, '.')-1 - instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email, '.')+1,length(Email)-instr(Email,'.')) not glob '*[^a-zA-Z]*'),
	Vorname							varchar[50] not null check(length(Vorname)>0 and Vorname not glob '*[^ -~]*'),
	Nachname						varchar[50] not null check(length(Nachname)>0 and Nachname not glob '*[^ -~]*'),
	Passwort						varchar[50] not null check(length(Passwort)>=5 and Passwort not glob '*[0-9][0-9]*'and Passwort glob '*[A-Z]*[A-Z]*')
	
);

create table IF NOT EXISTS Lieferant
(
	Email						varchar[50] primary key collate nocase check(length(Email)>0 and substr(Email, 0, instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email,'@')+1, instr(Email, '.')-1 - instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email, '.')+1,length(Email)-instr(Email,'.')) not glob '*[^a-zA-Z]*'),
	Haendlername					varchar[50] not null check(length(Haendlername)>0 and Haendlername not glob '*[^ -~]*'),
	foreign key(Email) references Benutzer(Email) on update cascade on delete cascade	
);

create table IF NOT EXISTS Ersatzteile
(
	ErsatzteilNr					int primary key check(ErsatzteilNr >= 0),
	Herstellerinfo					TEXT not null check(length(Herstellerinfo)>0 and Herstellerinfo not glob '*[^ -~]*'),
	Ersatz_Bezeichnung				varchar[50] not null check(length(Ersatz_Bezeichnung)>0 and Ersatz_Bezeichnung not glob '*[^ -~]*'),
	Bild							BLOB,
	KategorieBezeichnung			varchar[50] not null check(length(KategorieBezeichnung)>0 and KategorieBezeichnung not glob '*[^ -~]*'),
	foreign key(KategorieBezeichnung) references Kategorie(KategorieBezeichnung) on update cascade on delete cascade
);

create table IF NOT EXISTS stellt_bereit
(
	Email						    varchar[50] collate nocase check(length(Email)>0 and substr(Email, 0, instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email,'@')+1, instr(Email, '.')-1 - instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email, '.')+1,length(Email)-instr(Email,'.')) not glob '*[^a-zA-Z]*'),
	ErsatzteilNr					int check(ErsatzteilNr >= 0),
	Stueckpreis						double check(round(Stueckpreis, 2) = Stueckpreis and Stueckpreis > 0),
	primary key(Email, ErsatzteilNr),
	foreign key(Email) references Lieferant(Email) on update cascade
	on delete cascade,
	foreign key(ErsatzteilNr) references Ersatzteile(ErsatzteilNr)	on update cascade on delete cascade

);

create table IF NOT EXISTS Werkstatt
(
	WerkstattID						int primary key check(WerkstattID >= 0),
	Preis_pro_AW					float not null check(Preis_pro_AW > 0 and round(Preis_pro_AW, 2) = Preis_pro_AW),
	WerkstattName					varchar[50] not null check(length(WerkstattName)>0 and WerkstattName not glob '*[^ -~]*')
);

create table IF NOT EXISTS Ansprechpartner
(
	Email						varchar[50] primary key collate nocase check(length(Email)>0 and substr(Email, 0, instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email,'@')+1, instr(Email, '.')-1 - instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email, '.')+1,length(Email)-instr(Email,'.')) not glob '*[^a-zA-Z]*'),
	MA_Nr							int not null unique check(MA_Nr >= 0),
	WerkstattID						int not null check(WerkstattID >= 0),
	foreign key(WerkstattID) references Werkstatt(WerkstattID),
	foreign key(Email) references Benutzer(Email)	on update cascade on delete cascade
);

create table IF NOT EXISTS Modell
(
	ModellID						int primary key check(ModellID >= 0),
	Hersteller						varchar[50] not null check(length(Hersteller)>0 and Hersteller not glob '*[^ -~]*'),
	Modell_Bezeichnung				varchar[50] not null check(length(Modell_Bezeichnung)>0 and Modell_Bezeichnung not glob '*[^ -~]*'),
	Leistung_in_KW					int not null check(Leistung_in_KW > 0),
	Typ								varchar[50] not null check(length(Typ)>0 and Typ not glob '*[^ -~]*'),
	Klasse							varchar[50] not null check(Klasse like "PKW" or Klasse like "LKW" or Klasse like "KRAFTRAD")
);

create table IF NOT EXISTS Fahrzeug
(
	Kennzeichen						varchar[50] primary key collate nocase check(length(Kennzeichen)>0 and Kennzeichen not glob '*[^ -~]*'),
	Erstzulassung					datetime not null default(datetime('0000-00-00 00:00:00')) check(Erstzulassung is strftime('%Y-%m-%d %H:%M:%S', Erstzulassung)),
	HU_Datum						varchar[50] check(HU_Datum glob '[0-9][0-9][0-9][0-9]-0[1-9]' or HU_Datum glob '[0-9][0-9][0-9][0-9]-1[0-2]'),	
	Email						    varchar[50] collate nocase not null check(length(Email)>0 and substr(Email, 0, instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email,'@')+1, instr(Email, '.')-1 - instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email, '.')+1,length(Email)-instr(Email,'.')) not glob '*[^a-zA-Z]*'),
	ModellID						int not null check(ModellID >= 0),
	foreign key(Email) references Kunde(Email)	on update cascade on delete cascade,
	foreign key(ModellID) references Modell(ModellID)
);

create table IF NOT EXISTS Auftrag
(
	AuftragID						int primary key check(AuftragID >= 0),
	Auftragsdatum					datetime default(datetime('now')) not null check(Auftragsdatum is strftime('%Y-%m-%d %H:%M:%S', Auftragsdatum)),
	AW_Anzahl						int not null check(AW_Anzahl % 5 = 0 and AW_Anzahl > 0),
	Beschreibung					text not null check(length(Beschreibung)>0 and Beschreibung not glob '*[^ -~]*'),
	Email						    varchar[50] collate nocase not null check(length(Email)>0 and substr(Email, 0, instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email,'@')+1, instr(Email, '.')-1 - instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email, '.')+1,length(Email)-instr(Email,'.')) not glob '*[^a-zA-Z]*'),
	Kennzeichen						varchar[50] collate nocase not null check(length(Kennzeichen)>0 and Kennzeichen not glob '*[^ -~]*'),					
	foreign key(Email) references Ansprechpartner(Email) on update cascade on delete cascade,
	foreign key(Kennzeichen) references Fahrzeug(Kennzeichen) on update cascade on delete cascade
);

create table IF NOT EXISTS benoetigt
(
	ErsatzteilNr					int not null check(ErsatzteilNr >= 0),
	AuftragID						int not null check(AuftragID >= 0),
	primary key(ErsatzteilNr, AuftragID),
	foreign key(ErsatzteilNr) references Ersatzteile(ErsatzteilNr)	on update cascade on delete cascade,
	foreign key(AuftragID) references Auftrag(AuftragID)
);

create table IF NOT EXISTS Adresse
(
	AdressID						int primary key check(AdressID >= 0),
	Stadt							varchar[50] not null check(length(Stadt)>0 and Stadt not glob '*[^ -~]*'),
	PLZ								varchar[50] not null check(PLZ glob '[0-9][0-9][0-9][0-9][0-9]'),
	Strasse							varchar[50] not null check(length(Strasse)>0 and Strasse not glob '*[^ -~]*'),
	Hausnummer						varchar[50] not null check(Hausnummer glob '[0-9]' or Hausnummer glob '[0-9][0-9]' or Hausnummer glob '[0-9][0-9][0-9]' or Hausnummer glob '[0-9][0-9][0-9][0-9]' or Hausnummer glob '[0-9][0-9][0-9][0-9][a-z]' or Hausnummer glob '[0-9][0-9][0-9][a-z]' or Hausnummer glob '[0-9][0-9][a-z]' or Hausnummer glob '[0-9][a-z]')
);

create table IF NOT EXISTS ansaessig_bei
(
	AdressID						int primary key check(AdressID >= 0),
	WerkstattID						int  not null check(WerkstattID >= 0),
	foreign key(AdressID) references Adresse(AdressID),
	foreign key(WerkstattID) references Werkstatt(WerkstattID)
);

create table IF NOT EXISTS bewerten
(
	WerkstattID						int check(WerkstattID >= 0),
	Email							varchar[50]  collate nocase check(length(Email)>0 and substr(Email, 0, instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email,'@')+1, instr(Email, '.')-1 - instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email, '.')+1,length(Email)-instr(Email,'.')) not glob '*[^a-zA-Z]*'),
	Schulnote						int not null check(Schulnote between 1 and 6),
	primary key(WerkstattID, Email),
	foreign key(WerkstattID) references Werkstatt(WerkstattID),
	foreign key(Email) references Kunde(Email)	on update cascade on delete cascade
);

create table IF NOT EXISTS Kunde
(
	Email							varchar[50] primary key collate nocase check(length(Email)>0 and substr(Email, 0, instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email,'@')+1, instr(Email, '.')-1 - instr(Email, '@')) not glob '*[^a-zA-Z0-9]*' and substr(Email, instr(Email, '.')+1,length(Email)-instr(Email,'.')) not glob '*[^a-zA-Z]*'),
	Rueckrufnummer					int  check(Rueckrufnummer > 0),
	Werbeeinwilligung				boolean  not null,
	AdressID						int  not null check(AdressID >= 0),
	foreign key(Email) references Benutzer(Email)	on update cascade on delete cascade,
	foreign key(AdressID) references Adresse(AdressID)
);

create table IF NOT EXISTS wird_ersetzt
(
	ErsatzteilNr1					int primary key check(ErsatzteilNr1 != ErsatzteilNr2 and ErsatzteilNr1 >= 0),
	ErsatzteilNr2					int  not null check(ErsatzteilNr2 != ErsatzteilNr1 and ErsatzteilNr2 >= 0),
	foreign key(ErsatzteilNr1) references Ersatzteile(ErsatzteilNr)	on update cascade on delete cascade,
	foreign key(ErsatzteilNr2) references Ersatzteile(ErsatzteilNr)	on update cascade on delete cascade
);


--Ein Kunde soll nur Werkstätten bewerten können, bei denen auch ein Reparaturauftrag für diesen Kunden existiert.
create trigger "Auftrag nicht vorhanden" before insert on bewerten when(select werkstattid from werkstatt where new.werkstattid = (select werkstattid from ansprechpartner where ansprechpartner.Email = (select Email from auftrag where auftrag.kennzeichen = (select kennzeichen from fahrzeug where fahrzeug.Email = new.Email)))) is null begin select raise(abort, 'Kein Auftrag vorhanden') end; end;










