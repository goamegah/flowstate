# ========= STREAMLIT UI =========

# Docker Compose file
COMPOSE = docker compose
SERVICE = streamlit

.PHONY: ui-up ui-down ui-build ui-restart ui-logs

## Démarre l’interface UI
ui-up:
	$(COMPOSE) up -d $(SERVICE)

## Stoppe et supprime l'UI
ui-down:
	$(COMPOSE) down

## Rebuild propre de l’image UI
ui-build:
	$(COMPOSE) build $(SERVICE)

## Restart complet
ui-restart: ui-down ui-build ui-up

## Logs live
ui-logs:
	$(COMPOSE) logs -f $(SERVICE)

## Ouvre l’UI dans le navigateur
ui-open:
	xdg-open http://localhost:8501 || open http://localhost:8501 || echo "Ouvre ton navigateur sur http://localhost:8501"
