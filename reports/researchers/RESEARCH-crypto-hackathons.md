## Research Report: Cryptocurrency and Traditional Hackathon Platforms

### Executive Summary
Traditional hackathon platforms (Devpost, Devfolio, HackerEarth) primarily offer cash, cloud credits, and recruitment opportunities, with event discovery relying heavily on web scraping due to a lack of public APIs. In contrast, Web3/Cryptocurrency platforms (DoraHacks, Gitcoin, BeWater) offer decentralized prize models—including stablecoins, native Layer-1/Layer-2 tokens, and Quadratic Funding—and exhibit varying degrees of programmatic access, ranging from Gitcoin's open GraphQL endpoint to DoraHacks and BeWater's managed interfaces. Standard rules on all platforms are split between general terms of service and binding event-specific rules, with Web3 platforms introducing unique mechanisms such as wallet staking and Sybil-resistance verifications.

### Findings

#### Finding 1: Traditional Hackathon Platforms Overview (Devpost, Devfolio, HackerEarth)
Traditional hackathon platforms dominate corporate innovation and developer hiring pipelines. Devpost remains the industry standard, hosting global enterprise challenges for major tech companies. HackerEarth operates at the intersection of hackathons and technical hiring assessments, leveraging an automated coding engine to run challenges. Devfolio serves as a prominent community-focused gateway in India, hosting major events like ETHIndia. While Devfolio began as a traditional platform, it has heavily integrated Web3 capabilities, creating a hybrid model.
- Source: [Devpost Homepage](https://devpost.com/)
- Source: [Devfolio Discovery Portal](https://devfolio.co/discover)
- Source: [HackerEarth Challenges](https://www.hackerearth.com/challenges/)
- Confidence: High

#### Finding 2: Web3 and Cryptocurrency Hackathon Platforms Overview (DoraHacks, Gitcoin, BeWater)
Web3 hackathons are hosted on specialized decentralized portals that prioritize open-source collaboration, developer funding, and smart contract integrations. DoraHacks is a leading global hub hosting multi-chain developer initiatives and grants. Gitcoin operates as a decentralized network using the Allo Protocol, focusing on public goods and community-driven funding. BeWater is a builder-centric platform that coordinates hackathons, contests, and demo days specifically for emerging crypto ecosystems.
- Source: [DoraHacks Platform](https://dorahacks.io/)
- Source: [Gitcoin Explorer](https://explorer.gitcoin.co/)
- Source: [BeWater Platform](https://bewater.xyz/)
- Confidence: High

#### Finding 3: Prize Types and Financial Logistics
Prize types vary distinctly across both categories:
*   **Traditional (Devpost, Devfolio, HackerEarth)**: Prizes are primarily fiat cash (USD, INR, EUR) distributed via standard banking systems, combined with non-cash incentives like cloud credits (AWS, GCP, Azure), software licenses, hardware devices, and recruitment fast-tracks. Devfolio and HackerEarth also support token rewards for Web3 tracks.
*   **Web3 (DoraHacks, Gitcoin, BeWater)**: Prizes are predominantly decentralized assets. These include stablecoins (USDC, USDT, DAI) to protect against volatility, native blockchain tokens (ETH, L1/L2 tokens like TON, SOL, MATIC), project governance tokens, infrastructure credits (RPC endpoints, GPU instances like A100/H100), and Quadratic Funding (QF) matching pools (Gitcoin). Payment distribution usually requires wallet connections (e.g., Metamask) and occasionally KYC verification.
- Source: [Devpost Prizes and Rules](https://devpost.com/terms)
- Source: [Devfolio Wallet Connection and Prizes](https://devfolio.co/terms)
- Source: [DoraHacks Hackathon Guidelines](https://dorahacks.io/)
- Confidence: High

#### Finding 4: Event Discovery and API Accessibility
Programmatic event discovery is highly restricted on traditional platforms but accessible on select Web3 platforms:
*   **Devpost, HackerEarth, and Devfolio**: None of these platforms expose public GraphQL or REST APIs for general hackathon listings. Devfolio offers an SDK for applications (`sdk.js`) and a Model Context Protocol (MCP) server for participant workspace integration, but not for event queries. Discovering listings programmatically requires custom web scraping (which Devpost explicitly prohibits in its Terms of Service) or third-party web scrapers (such as Apify actors).
*   **DoraHacks and BeWater**: Neither platform offers a public GraphQL API for event discovery. Programmatic retrieval relies on scraping the frontend or inspecting network tabs to capture internal REST API requests.
*   **Gitcoin**: Provides high accessibility. Developers can query rounds, projects, and applications programmatically using Gitcoin's public Grants Stack Indexer GraphQL API. An interactive playground is hosted on their platform. Processed datasets can also be downloaded in Parquet format from their open data portal.
- Source: [Gitcoin Grants Stack Indexer GraphQL](https://grants-stack-indexer-v2.gitcoin.co/graphql)
- Source: [Gitcoin Data Portal](https://grant-data.xyz/)
- Source: [Devfolio MCP Documentation](https://mcp.devfolio.co/)
- Confidence: High

#### Finding 5: Rules and Terms & Conditions (T&C) Dissemination
Hackathon rules are consistently split into general platform terms and specific event rules:
*   **Devpost & HackerEarth**: General platform Terms of Service (T&C) govern user behavior, account creation, and plagiarism limits. Event-specific rules are listed on the dedicated hackathon microsites under a "Rules" or "Official Rules" tab. These rules act as a binding contract and override platform terms.
*   **Devfolio**: Implements platform Code of Conduct. Some events require a refundable deposit (wallet staking in ETH or INR) to ensure serious participation, which is returned upon submitting a valid project.
*   **DoraHacks & BeWater**: Event rules, tracks, and judging criteria are found under the "Rules" or "Guidelines" tabs on each campaign landing page. In addition, real-time rule changes and technical announcements are decentralized into Telegram groups and Discord channels.
*   **Gitcoin**: Incorporates anti-Sybil rules. To participate and receive grant matching, users must verify their identity using Gitcoin Passport, which collects cryptographic credentials across Web2 and Web3 accounts.
- Source: [Devpost Terms of Service](https://devpost.com/terms)
- Source: [Devfolio Terms of Service](https://devfolio.co/terms)
- Source: [HackerEarth Terms of Service](https://www.hackerearth.com/terms-of-service/)
- Source: [Gitcoin Passport Information](https://passport.gitcoin.co/)
- Confidence: High

### Recommendations

1.  **Recommended for Programmatic Event Discovery**: Use **Gitcoin** if you need automated event and grant discovery because it is the only platform that provides a fully public, documented, and community-supported GraphQL API (`https://grants-stack-indexer-v2.gitcoin.co/graphql`) alongside the GraphiQL playground (`https://grants-stack-indexer-v2.gitcoin.co/graphiql`). This eliminates the risk of account bans and code breakage associated with web scraping other platforms.
2.  **Recommended for Traditional Hackathon Participation**: Use **Devpost** for fiat-incentivized or enterprise-grade hackathons because of its market dominance, large community, and well-structured, legally binding "Official Rules" tab found on each microsite.
3.  **Recommended for Multi-Chain Web3 Hackathons**: Use **DoraHacks** for crypto-native hackathons due to its extensive list of ecosystem sponsors (such as TON, Cosmos, Solana) and diverse track prizes (native tokens, stablecoins, and venture funding), while using their official Discord/Telegram communities to receive real-time rule changes.

### Sources

1.  [Devpost Terms of Service](https://devpost.com/terms) - accessed 2026-05-21
2.  [Devfolio Terms of Service](https://devfolio.co/terms) - accessed 2026-05-21
3.  [HackerEarth Terms of Service](https://www.hackerearth.com/terms-of-service/) - accessed 2026-05-21
4.  [Gitcoin Terms of Service](https://gitcoin.co/terms) - accessed 2026-05-21
5.  [Gitcoin Grants Stack Indexer GraphQL Playground](https://grants-stack-indexer-v2.gitcoin.co/graphiql) - accessed 2026-05-21
6.  [Gitcoin Data Portal](https://grant-data.xyz/) - accessed 2026-05-21
7.  [BeWater Platform](https://bewater.xyz/) - accessed 2026-05-21
8.  [DoraHacks Platform](https://dorahacks.io/) - accessed 2026-05-21
