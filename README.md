
```mermaid
flowchart TD

%% ===== FRONTEND ===== %%
subgraph Frontend [Frontend Layer]
    UI[HTML_UI\nUser Input & Result Display]
    JS[JavaScript_Core\nHandle Events / Fetch API]
    CSS[CSS_Styles\nLayout & Visual Design]
end

%% ===== BACKEND ===== %%
subgraph Backend [Backend Layer]
    A[SearchEngineApplication\nMain Entry]
    C[SearchController\nAPI Endpoint]
    T[TranslationHandler\nChinese → English]
    O[OneHotEncoder\nKeyword Vectorization]
    Q[CosmeticQuery\nSearch Core Service]

    subgraph QueryModules [CosmeticQuery Submodules]
        CR[Crawler\nFetch Web Pages]
        H[HtmlHandler\nParse HTML Content]
        K[KeywordExtractor\nExtract & Count Keywords]
        R[ProductRanker\nWeighted Scoring]
        F[ResultFormatter\nFormat JSON Output]
    end
end

%% ===== DATA FLOW ===== %%
UI -->|"User enters query"| JS
JS -->|"POST /api/search"| C
C -->|"Translate to English"| T
T -->|"Return English Query"| C
C -->|"Convert to Keyword Map"| O
O -->|"Send Keyword Tokens"| Q
Q -->|"Request Web Pages"| CR
CR -->|"Send HTML"| H
H -->|"Extract Text & Links"| Q
Q -->|"Analyze Keyword Frequency"| K
K -->|"Return TF Map"| R
R -->|"Calculate Weighted Score"| F
F -->|"Return JSON Results"| C
C -->|"Send Results"| JS
JS -->|"Render Data"| UI
UI -->|"Apply Styles"| CSS

%% ===== STYLE ===== %%
classDef front fill:#f5f5f5,stroke:#999,stroke-width:1px;
classDef back fill:#f0fff0,stroke:#999,stroke-width:1px;
classDef sub fill:#e8f6e8,stroke:#888,stroke-width:1px;
class UI,JS,CSS front
class A,C,T,O,Q back
class CR,H,K,R,F sub
linkStyle default stroke:#555,stroke-width:1.2px,color:#222;
```