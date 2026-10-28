# Screenshots and Project Links

## Repositories

- [Backend repository](https://github.com/hannafr14/Tierras-de-Asturias-backend)
- [Frontend repository](https://github.com/hannafr14/Tierras-de-Asturias-Frontend)

## Design and Jira

| Resource | Link |
|---|---|
| Desktop mockup | [Figma: desktop](https://www.figma.com/design/KIWo4qY828gz9moQC9s26L/Untitled?node-id=0-1) |
| Mobile design | [Figma: mobile](https://www.figma.com/design/KIWo4qY828gz9moQC9s26L/Untitled?node-id=38-1251) |
| Wireframe | [Figma: wireframe](https://www.figma.com/design/KIWo4qY828gz9moQC9s26L/Untitled?node-id=38-1252) |
| Jira backlog | [PPTDA board 67, with PPTDA-392 selected](https://hannafrolova14.atlassian.net/jira/software/projects/PPTDA/boards/67/backlog?selectedIssue=PPTDA-392) |

Links were supplied by the project author. Access permissions and Jira completion statuses have not been verified. Evaluators need permission to view private resources. Design links describe the intended interface, not guarantees of API functionality.

## Pending Information

- Publishing destination: GitHub Wiki or a Confluence space.
- Deployment URLs when available.

Creating these Markdown files does not publish pages or change Jira statuses.

## Useful Images

The backend does not need product photographs to explain its architecture. [Architecture and Data](Architecture-and-Data.md) already includes a Mermaid diagram.

### Available Testing Evidence and Diagram

The screenshots supplied on 2026-10-28 are stored in `docs/images/` and embedded in [Operations and Testing](Operations-and-Testing.md#supplied-test-results). The original planned data-model diagram is already embedded in [Architecture and Data](Architecture-and-Data.md#planned-data-model).

| File | Content |
|---|---|
| [backend-tests.png](../images/backend-tests.png) | VS Code test summary: 196 of 196 tests passed |
| [backend-coverage.png](../images/backend-coverage.png) | JaCoCo snapshot: 88% instructions and 67% branches |
| [planned-data-model.png](../images/planned-data-model.png) | Original target data model, including future functionality |
| [class-diagram.png](../images/class-diagram.png) | Implemented domain class diagram: seven JPA entities, fields, selected methods, and multiplicities |

The class diagram also has a [scalable SVG](../images/class-diagram.svg) and an [editable Mermaid source](../diagrams/class-diagram.mmd). It is documented separately from the planned data model in [Architecture: Domain Class Diagram](Architecture-and-Data.md#domain-class-diagram).

### Optional API Screenshots

The following screenshots can also be stored in `docs/images/`, but have not yet been supplied:

| File | Content |
|---|---|
| `api-products.png` | Catalog GET request with a 200 response |
| `api-validation.png` | Fictitious invalid request and 400 response |
| `api-my-orders.png` | Demonstration own-order history |

API image embeds can be added when those files are available. For testing evidence, retain the generated reports as well as screenshots; the supplied images do not establish that the test summary and coverage came from the same run.

## Privacy

Hide passwords, cookies, CSRF tokens, environment keys, and real names, addresses, and telephone numbers. Review Postman's cookie and header tabs before capturing.

Do not attach SQL dumps or environment files. Use fictitious example data only; do not publish credentials for a working account.

Catalog and checkout screenshots are documented in the frontend Wiki. They must show the real application and remain distinct from the intended design.
