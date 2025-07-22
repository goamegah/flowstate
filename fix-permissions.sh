#!/bin/bash

# Répertoire racine du projet (à adapter si besoin)
PROJECT_DIR="/home/goamegah/Documents/develop/esgi/4a/s2/spark/projet/flowstate"

echo "=>  Correction des permissions dans le dossier target..."

# Réassignation des droits utilisateur sur le dossier target
sudo chown -R $USER:$USER "$PROJECT_DIR/target"
sudo chown -R $USER:$USER "$PROJECT_DIR/shared"

# Optionnel : nettoyage si nécessaire (décommente la ligne suivante si tu veux clean automatiquement)
# echo "=> Suppression du dossier target pour repartir proprement..."
# rm -rf "$PROJECT_DIR/target"

echo "########## Permissions corrigées ! Tu peux relancer ton application dans IntelliJ. ##########"
