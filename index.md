# EFSP Operations Manual

## Overview

EFSP (Electronic Filing Service Provider) is a proxy between clients such as DocAssemble and an [ECF 4.0 (LegalXML's Electronic Court Filing 4.0 standard)](https://docs.oasis-open.org/legalxml-courtfiling/specs/ecf/v4.0/ecf-v4.0-spec/ecf-v4.0-spec.html) EFM (Electronic Filing Manager), such as Tyler. The EFSP is commonly used to create court filings. This application (EfileProxyServer) fulfills the EFSP role and when EFSP is mentioned in this document, you can assume it is referring to this application.

## Design Approach

Try to keep code changes minimal. Keep backwards-compatibility and allow new features to be controlled by additional environment variables. When possible, allow the features to be used beyond just the platform/database stack (Fly.io/Supabase) we're using.

## Staging/Production

There are two environments for EFSP. The staging environment is safe to test against and is configured to communicate w/ the staging Tyler EFM. Care should be taken as emails & SMS messages are still sent. This should be fine as long as you're testing with your own contact information. The production environment is shared between many DocAssemble servers. In the future, this may be partitioned into jurisdictions for scalability and isolation.

## Comparison to Previous Setup on AWS Lightsail

The previous staging & production environments ran in AWS Lightsail. EFSP ran as a dockerized application on a 4GB Ubuntu instance. Docker Compose was used to manage the setup. The Java application and Postgres database server were managed through Docker Compose and both ran within the same instance.

Most operational tasks were performed by SSH'ing into the machine and executing commands in the CLI (Command-Line Interface). The source code was installed and updated on the machine directly using Git. Logs were stored directly in the local filesystem, with API access through the EFSP application to provide visibility to clients who did not have SSH access.

Data is stored within two separate databases: user_transactions and tyler_efm_codes. The tyler_efm_codes database can be rebuilt from scratch. The user_transactions database contains critical state and needs to be preserved.

The Quartz Scheduler is embedded to run Tyler Code updates on a daily schedule at around 2:15 am (server time, default ET for BOS). The scheduler is not clustered and in-memory. However, it is relatively safe for multiple updates to be running in separate processes, as the database will sort out the locking/blocking and the tyler_efm_codes database will be updated correctly regardless.

SSL/TLS is handled by Let's Encrypt. However, the certificate renewal is currently a manual process that involves a bit of downtime. 

## Fly.io Stack

The updated stack uses Fly.io, Supabase, Papertrail, and Cloudflare.

* Fly.io runs the dockerized EFSP application.
* Supabase is the Postgres database.
* Papertrail serves as the cloud-hosted log management system, making it easy to aggregate, manage, and share logs.
* Cloudflare provides SSL/TLS encryption and DNS. (TODO: are we planning to use the DDoS protection & CDN capabilities? assuming no for now.)

The overall goal is to make it easier to run and operate EFSP as a service, with the tradeoff being a small monthly cost. Web interfaces are favored over the CLI (Command-Line Interface), although there is still lots of CLI and all existing commands are supported. Ideally, the learning curve will be shortened and day-to-day operational tasks can be done without needing to use the CLI.

The current setup keeps a single machine running at all times within the Boston region. Because Fly.io, Supabase, Papertrail, and Cloudflare are all cloud services, scaling up and out is pretty easy. The trade-off is one of increased cost as more scale is requested. Fly.io also supports auto-scaling. Given the current volume, scaling was not explored and we opted for the simplest conceptual model of a single, continuously instance of the application.

The EFSP application itself is mostly clusterable, the only exception being the way the Tyler EFM Code Updates are scheduled right now. For a single, always running instance, nothing needs to be done. In a scale-out scenario, the simplest approach of disabling the updates is supported. The EFSP application itself is stateless and will use the shared data within the database, so the single updater will result in every instance seeing the latest EFM codes. 

## Secrets/Configuration

All the supported variables are enumerated in the env.example file. Most of these are the same between the Lightsail and Fly.io setups. There are new variables for Papertrail and to configure the Quartz Scheduler. Details are in the env.example file.

You should have separate .env files for each environment. For example, staging values would be stored in a .env.staging and production values in .env.production.

## First Time Fly.io Setup

This section covers the steps for spinning up a brand-new Fly.io app.

### Pre-requisites

You should already have the following:
* An .env file with the secrets and environment configuration appropriate for the new app. The docs will refer to this as .env.fly but you can name it whatever makes sense and substitute the name when you see it in the example commands.
* A Fly.io account
* [flyctl](https://fly.io/docs/flyctl/), the Fly.io CLI tools, installed

#### Create the new app

You will need to create the app before deploying. Since the fly.toml already exists, you will want to create the app without generating a new one. To create the app, you will need an app name and Fly.io organization. The example commands will reference efsp-staging and the suffolk-lit-lab organization, but you should substitute your own values there. You can use the same app name, but the Fly.io organization will likely be different.

Create the app by running:
```
fly app create efsp-staging --org suffolk-lit-lab
```

You should see a message saying:
```
New app created: efsp-staging
```

Next, configure the app with your .env values. You should have already copied the env.example file as .env.fly and edited the values in it to match your environment.
```
cat .env.fly | fly secrets import --app efsp-staging
```

Now deploy the application by running:
```
fly deploy --config fly.toml --app efsp-staging
```
You can omit the --app parameter if you're using the app name defined within the fly.toml. You can also omit the --config parameter if you're using the default fly.toml file (as opposed to fly.production.toml, for example). The shortened version in that case would be:
```
fly deploy
```

This step will take some time as Fly.io verifies the configuration and builds the cloud virtual machines using a cloud-based Docker system. After the build is complete, Fly.io will push the image to its registery, whose name should start with registry.fly.io.

If all goes well, your app will be running. Your app should be reached by https on a Fly.io domain name. This value will look something like https://{your-app-name}.fly.dev/ and should be in the output.

Finally, set the scale to limit the number of machines created to 1. By default, Fly.io will create 2 machines for high availability. For reasons discussed in the scaling section, EFSP has opted to use a single, always running machine.
```
fly scale --app efsp-staging count app=1
```


## Deploying Code Updates

### Manual Deployment

The process for manual deployment is very similar to the non-Fly.io steps.

First, update the code on your local to the latest:
```
git fetch --all
git pull origin main
```

Then run:
```
fly deploy -c {MY_FLY.TOML}
```
Where the {MY_FLY.TOML} value is the fly.toml config file for the environment you are deploying to. If you don't pass a -c option, the default fly.toml file will be used. For EFSP, this is the staging environment. The production config file is fly.production.toml.

To deploy to production:
```
fly deploy -c fly.production.toml
```

### Auto-Deploy

If you have forked the EFSP repo, you can set up auto-deploy. For more information on how you would do this, check out:
https://fly.io/docs/app-guides/continuous-deployment-with-github-actions/


## Viewing Logs

You can view the logs directly on Fly.io. The application logs are also shipped over to Papertrail, which will be covered in more detail. Papertrail is the recommended interface to view logs because it has search, filtering, and aggregation capabilities. However, checking on Fly.io is helpful if you are troubleshooting, as you will be able to see the platform logs in additional to the applications logs. Plus even if there is an error preventing the application from sending logs to Papertrail, the information will be stored within Fly.io's logs.

To view the logs within Fly.io, click on "Live Logs" in the side menu.

![Fly.io Dashboard Side Menu](./fly_dashboard_side_menu.png)

You should now see a web view displaying the live logs.

![Fly.io Live Logs](/Users/stng/projects/EfileProxyServer/docs/operations_guide/fly_live_logs.png)

For more information on viewing metrics and logs on Fly.io, check out:
https://fly.io/docs/metrics-and-logs/


## Reference Guide


https://fly.io/docs/metrics-and-logs/
TODO: view logs, filter/search

TODO: how papertrail is setup - groups, machines, TLS Syslog (host+portname+TCP (TLS)). we allow dynamic hosts, but quarantine them into unrecognized

### Adding New System to Papertrail

Find the group in papertrail, get the host + port. We allow for unrecognized systems for flexibility. If we see log spam, we can ban the system.


### Scaling Up Fly.io

You can scale up the # of machines if you are reach a point where there is a lot of load. The only caveat is that the Tyler EFM Code Updater, which runs on a schedule by default, can get into a conflict if there are multiple instances doing the update at the same time. Since the Quartz Scheduler is running in-memory by default and the schedule is fixed in the code, it is safest to disable the automated updates on any supplemental machines. See env.example for more details on how to do that. 

The default Fly config for EFSP will keep just a single machine running at all times. This is important as the code update will only happen is an instance is active when the schedule hits (2:15 am on the machine's clock). 

You can set the # of instances to scale to with the following command:
```
fly scale count app={NUMBER_OF_INSTANCES}
```
where NUMBER_OF_INSTANCES is 1 or higher. For simplicity, we recommend setting it to 1.

Generally speaking, you will want to have pre-created machines that are stopped. This optimizes the cold start time, although it will still be relatively slow since EFSP is a Java application. Expect a pre-created machine's cold start to take about 5 seconds. Creating a new machine will be an order or two of magnitude longer. Pre-created machines that are stopped cost very little as you are only billed for the storage space. The cost is negligible compared to the cost of a running machine.

Properly scaling EFSP using Fly.io is an advanced topic beyond the scope of this guide. You should have a good understanding of Fly.io's architecture before attempting this. Here are some good reference links to start from:
* https://fly.io/docs/apps/scale-machine/#scale-vm-memory-and-cpu-with-flyctl
* https://fly.io/docs/flyctl/scale/
* https://fly.io/docs/reference/autoscaling/#main-content-start
* https://fly.io/docs/apps/autostart-stop/


### Viewing Metrics for Your Fly.io Application

Fly.io collects metrics such as memory usage, CPU utilization, etc. To view your metrics, go to the dashboard. Select "Metrics" from the side menu on the left.

![Fly.io Dashboard Side Menu](./fly_dashboard_side_menu.png)

You will now see Fly.io's managed Grafana page. This gathers together the metrics collected from your application and provides easy to understand visualizations.

![Grafana Dashboard](./fly_grafana_metrics.png)

To learn more about Fly.io metrics, check out:
https://fly.io/docs/metrics-and-logs/metrics/#dashboards

To learn more about Grafana, check out:
* https://grafana.com/docs/grafana/latest/dashboards/
* https://grafana.com/docs/grafana/latest/panels-visualizations/visualizations/

### Generating a New API Key

Run the fly_create_api_key.sh script to generate a new API key. The generated key will grant API access to your EFSP instance. The API key is printed to the console output and can be copied from there. Best practice is to give each user of your EFSP instance their own API key.

To create an API key for the app specified in your fly.toml, run:
```
./fly_create_api_key.sh
```

If you want to create a production key, run:
```
./fly_create_prod_api_key.sh
```

Both scripts are the same except for the Fly.io config file passed to the command. You can also create more copies for any of your environments, or alter the script to take in the app or config name. Two different script files are used to make it easier and more obvious for the user running the command. 

The script uses Fly.io commands to spin up an ephemeral machine that will run the Java code to generate the API. This method leverages Fly.io as the authorization mechanism and keeps the permission to generate new API keys to the set of authorized users. An ephemeral machine is used so that key generation does not impact the API performance.  

### SSH Console access

Fly.io offers a Fly command that is akin to connecting to a Lightsail instance using SSH. This is handy if you need to troubleshoot the machine. Unlike SSH, you won't need to set up any keypairs. Instead, your Fly.io login will be all that is needed to authenticate you. To SSH to a Fly.io machine, use the following command:
```
fly ssh console
```

### Setting Secrets/Configuration in Fly.io

You can view the names of the variables that are set with the following command:
fly secrets list

Note that this will not show the values, but it does display both the digest and creation date for the value.

The recommended way to set secrets in Fly.io is to edit them in a .env file. The name .env.fly is assumed for the remainder of these instructions. Note that .env files are excluded from Git. This is important as you should never commit secrets into the repository. TODO: add location to store secrets

After you edit your .env.fly file and set the appropriate values, you can use the following command to update the Fly.io secrets:
```
cat .env.fly | fly secrets import
```

This will sync the value of every variable in the .env.fly to Fly.io. Note that this will not unset/touch any secrets whose names are not in the .env.fly file.

You can manually set a single secret with the following command:
```
fly secrets set [name] [value]
```

To unset a value, use the following command:
```
fly secrets unset [name]
```

### Supabase Customizations
The "Enforce SSL on incoming connections" is set to true. Since Fly.io and Supabase are not within the same location, this setting ensures that traffic between the two is secured.
We disabled the pg_graphql extension and API access as RLS (Role-Level Security) is not set up for EFSP databases.
TODO: network restrictions

### Backing Up the Database

The Supabase Postgres database is automatically backed up on a daily basis.

### Restoring a Database Backup
Go the Database -> [Platform] Backups (TODO: add screenshot). You will see a list of all the available backups. Click on the "Restore" button. This will bring up the confirmation dialog. Select "Confirm Restore" to restore the backup.

### Viewing Data in the Database

You can view database in the Postgres database through Supabase's web interface. Go to the Table Editor (TODO: add screenshot). Here you can see all the tables within the "postgres" database. Database in this context refers to the database within Postgres's database/schema/table concept, not as the generic term. The web view can only show tables within the "postgres" database. For the EFSP Fly.io setup, both POSTGRES_CODES_DB and POSTGRES_USER_DB are set to "postgres" so that the data is surfaced within Supabase's interface. Future development work could allow the schema to be configurable, in which case the data could be organized into two different schemas, both of which are accessible in the web interface. In Lightsail, two different databases were used:  tyler_efm_codes and user_transactions, respectively. You can also configure your .env.fly to use those databases. This will work fine on the application side, but you will lose visibility in the web interface.

### Connecting to Supabase with Your Own Database Tools

You can also use your own database tools, such as [PgAdmin](https://www.pgadmin.org/) or psql, to interact with the data in your Supabase database. To get the information for the connection string, go to "Project Settings", then click on "Database" under the "Configuration" section. You should now see a page with the connection string on the right.

For more details about connecting to Supabase with your own database tools, check out:
https://supabase.com/docs/guides/database/connecting-to-postgres#direct-connections
