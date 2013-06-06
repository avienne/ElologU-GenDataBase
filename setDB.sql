CREATE TABLE APP.eclairage
(
    heure CHAR(22) NOT NULL PRIMARY KEY,
    consommation FLOAT
);
CREATE TABLE APP.eau
(
    heure CHAR(22) NOT NULL PRIMARY KEY,
    consommation FLOAT
);
CREATE TABLE APP.ventilation
(
    heure CHAR(22) NOT NULL PRIMARY KEY,
    consommation FLOAT
);
CREATE TABLE APP.electricite
(
    heure CHAR(22) NOT NULL PRIMARY KEY,
    consommation FLOAT
);
CREATE TABLE APP.chauffage
(
    jour DATE NOT NULL,
    heure TIME NOT NULL,
    numCapteur INT NOT NULL,    
    consommation FLOAT,
    PRIMARY KEY(jour, heure, numCapteur)
);
CREATE TABLE APP.configurations 
(
    mode VARCHAR(8) NOT NULL,
    attribut VARCHAR(15) NOT NULL,
    valeur VARCHAR(60),
    PRIMARY KEY(mode, attribut)
);
CREATE TABLE APP.notifications 
(
    id INT NOT NULL PRIMARY KEY 
    GENERATED ALWAYS AS IDENTITY
    (START WITH 1, INCREMENT BY 1),
    gravite VARCHAR(10) NOT NULL,
    heure CHAR(22) NOT NULL,
    action VARCHAR(20) NOT NULL,
    equipement VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL
);
