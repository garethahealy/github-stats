MEMBER_MAPPING_REPO ?= git@github.com:redhat-cop-dev/member-mapping.git
MEMBER_MAPPING_DIR ?= member-mapping
MEMBER_MAPPING_CSVS := ldap-members.csv supplementary.csv

.PHONY: help member-mapping creds

help:
	@echo "Usage:"
	@echo "  make member-mapping"
	@echo "  eval $$(make creds)"
	@echo ""
	@echo "Targets:"
	@echo "  creds           Print a command to source creds"
	@echo "  member-mapping  Clone/update member-mapping and move CSVs"

member-mapping:
	@if [ ! -d "$(MEMBER_MAPPING_DIR)/.git" ]; then \
		git clone "$(MEMBER_MAPPING_REPO)" "$(MEMBER_MAPPING_DIR)"; \
	else \
	    git -C "$(MEMBER_MAPPING_DIR)" reset --hard && \
		git -C "$(MEMBER_MAPPING_DIR)" fetch --all --prune && \
		git -C "$(MEMBER_MAPPING_DIR)" pull; \
	fi
	@for csv in $(MEMBER_MAPPING_CSVS); do \
		if [ -f "$(MEMBER_MAPPING_DIR)/$$csv" ]; then \
			mv -f "$(MEMBER_MAPPING_DIR)/$$csv" "./$$csv"; \
		else \
			echo "Missing $(MEMBER_MAPPING_DIR)/$$csv"; \
		fi; \
	done

creds:
	@echo "source scripts/_creds.sh"
