# Product Data Audit: 2026-10-25

The audit covers all 337 seed products in `src/main/resources/data/products.json`.

- Replaced 45 broken remote product images with local copies in the frontend repository.
- Replaced the Los Arandanos juice quality-seal illustration with an actual bottle photo.
- Filled 15 previously zero prices using matching publicly advertised offers in EUR.
- Left 53 prices unresolved rather than assigning prices from a different producer, portion, weight or case.
- Preserved product names, ordering, categories, quantities, sizes and all existing positive prices.

## Sources And Limitations

See [product-price-sources-2026-10-25.json](product-price-sources-2026-10-25.json) for each price, seller, selling unit and unresolved product. Retailer prices are reference prices, not quotes from the producers. They may change and do not include delivery unless the seller explicitly says so.

Image attribution, source pages and checksums are in `public/images/products/sources.json` in the frontend repository. Public WordPress CDN copies were used where the original producer image host was unavailable. No generated product photographs were used. Two Outurelos images are representative photos reused by the seller across weights/seasons, not verified pack-specific photos. The Prau Monga photograph shows a single bottle while the product name and price still describe a six-bottle case.

Copyright and trademark rights remain with their owners. Permission for republication has not been verified; attribution is not a licence. Confirm permission before public/commercial publication.

## Existing Database

These file changes do not migrate an existing database. `DataSeeder` deliberately skips existing products. Restarting the backend does not apply the corrected images or prices to those rows.

Before applying the changes to a working database:

1. Obtain approval and create a fresh SQL backup outside both repositories.
2. Match products by exact product name and the associated farm, not array positions or guessed database IDs.
3. Update only the reviewed image and previously zero price fields, using expected old values as guards.
4. Preserve IDs, categories, stock, users, orders and order items. Verify row counts before committing the transaction.
5. Deploy the frontend assets together with the new `/images/products/` URLs.

### Local Database Application

Applied on 2026-10-25 after explicit approval and a fresh complete SQL backup in the workspace-level `backups/` directory, outside both repositories.

- Updated 59 existing products: 46 image fields and 15 previously zero prices.
- Verified all 337 products through the running API and loaded all 46 local images in a browser.
- Checked a live product page at desktop and mobile sizes.
- Verified every other product field against the pre-update snapshot, and checksums of all other database tables remained unchanged.
- No schema, users, orders, order items, categories or stock were modified. The 53 unresolved prices remain unchanged.

The private backup, transaction checks and verification output are local operational artifacts, not files to commit. The JSON and image files must still be versioned in their respective repositories when ready.

An unresolved zero price means missing data, not a free product. Do not enable real sales of these products until a valid selling unit and price are confirmed.

## Follow-Up: 2026-10-27

Reviewed the 16 farms with no visible priced products. Added nine distinct products and filled the previously zero price of the existing Oro D'Ibias wine. Eight more farms now have priced products: Temia, Ca Sanchu, ReinAstur, La Boalesa, Bernueces, Taramundi, Demues and Vina Cuedo.

See [farm-products-sources-2026-10-27.json](farm-products-sources-2026-10-27.json) for individual offers, pack sizes, image sources and the eight unresolved farms. Prices were checked against live seller pages; ReinAstur's 500 g prices were checked against the selected Shopify variants, not the lower starting price for a smaller jar. Retailer and promotional prices are educational reference data, not producer quotations or a live stock feed.

- There are now 346 seed/database products, including 294 with positive prices and 52 still unpriced.
- Added the Demues 300 g wedge separately; did not assign a portion price to the existing unspecified whole-cheese entry. Its visible listing says 300 g, despite inconsistent 350 g JSON-LD metadata.
- Left La Penona's 300 g products unchanged because the found offer describes a 360-420 g piece. Left Casomera unchanged because its site says the 2026 blueberry season has ended.
- Classified Bernueces Sidradulce as alcohol-free apple juice (`Zumos`), not alcoholic cider.
- New records follow the existing demo seed convention `quantity: 1`. This is not a claim of confirmed physical inventory. All existing quantities and positive prices were preserved.
- Downloaded ten actual product photographs. The frontend's `public/images/products/sources-2026-10-27.json` records their URLs, file paths and SHA-256 checksums. The rights notice above still applies.

Applied the same reviewed changes to the working database after a fresh complete SQL backup outside the repositories. Exact farm/name matches and guarded transactional writes were used. All 336 other original product rows remained unchanged; the wine retained its ID, name, description, category and quantity. Checksums of all eight non-product tables remained unchanged, including users, orders, order items, farms, categories and persistent logins. Private backups and operational scripts stay outside versioned documentation (the scripts are under ignored `target/`).

### Corias Image Follow-Up

All ten Bodega Monasterio de Corias product image URLs returned valid images to the download client, but failed in Chromium with `ERR_HTTP2_PROTOCOL_ERROR`, causing the frontend to display placeholders. Saved unmodified local copies and changed only their image paths in the seed JSON and working database, after another fresh backup. No prices, descriptions, sizes, quantities, IDs or relationships were changed. All non-product table checksums and every other product field were verified unchanged.

The frontend's `public/images/products/sources-corias-2026-10-27.json` records the original producer URLs, local paths and SHA-256 checksums. All ten assets are actual PNG product photos. The wine jelly's original SVG was a generic basket illustration; replaced it with the actual jar photograph from the individual producer product page. Verify republication rights before public/commercial use.

### Pamirandi Demo Menu Price

With the user's explicit approval, added `Sidra natural Pamirandi` at 3 EUR using the Casa Niembro restaurant menu reproduced on Restaurant Guru. This is an educational demo price, not a confirmed current retail bottle price. The reproduced owner menu is labelled as a year old. No serving or bottle volume is specified, so both size fields remain null. The new record uses the established demo quantity of 1, not confirmed physical stock.

See [pamirandi-menu-reference-2026-10-27.json](pamirandi-menu-reference-2026-10-27.json) for the price context and producer source. The producer's unlabelled bottle-and-glass photo is stored locally; frontend attribution is in `public/images/products/sources-pamirandi-2026-10-27.json`. It is not a pack-specific photograph. The earlier unresolved Pamirandi audit entry remains historical; this addition does not establish a verified retail price.

The seed and working database now contain 347 products, 295 with positive prices and 52 unpriced. This follow-up adds just one product to the existing farm and Sidra category after a fresh database backup. Existing product rows and all non-product tables are unchanged.
