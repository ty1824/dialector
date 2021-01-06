'use strict';

import * as fs from "fs"
import * as path from 'path';
import * as net from 'net';
import * as child_process from "child_process";

import { workspace, Disposable, ExtensionContext } from 'vscode';
import { LanguageClient, LanguageClientOptions, SettingMonitor, StreamInfo, ServerOptions, TransportKind } from 'vscode-languageclient';
import { Console } from "console";

// this method is called when your extension is activated
// your extension is activated the very first time the command is executed
export function activate(context: ExtensionContext) {
	console.log("Activating");
	let connectionInfo = {
		port: 5008
	}

	let serverOptions = () => {
		let socket = net.connect(connectionInfo)
		socket.on("connect", () => console.log("connected"))
		return Promise.resolve({
			writer: socket,
			reader: socket
		})
	}
	/*
	function createServer(): Promise<StreamInfo> {
		return new Promise((resolve, reject) => {
			var server = net.createServer((socket) => {
				console.log("Creating server");

				resolve({
					reader: socket,
					writer: socket
				});

				socket.on('end', () => console.log("Disconnected"));
			}).on('error', (err) => {
				// handle errors here
				throw err;
			});

			console.log("Server created?")
			console.log(server)

			var i = 0
			while (i < 100000000) {
				i++
			}

			// grab a random port.
			server.listen(() => {
				// Start the child java process
				let options = { cwd: workspace.rootPath };
				let address = server.address()

				let executablePath = path.resolve(context.extensionPath, '..', 'server', 'build', 'install', "server", "bin", "server.bat")
				console.log("Path: " + executablePath)
				let args = [
					(address as net.AddressInfo).port.toString()
				]

				let process = child_process.spawn(
					executablePath, 
					args, 
					options
				);
				console.log(process)

				// Send raw output to a file
				if (!fs.existsSync(context.storageUri?.fsPath!))
					fs.mkdirSync(context.storageUri?.fsPath!);

				let logFile = context.storageUri?.fsPath! + '/glottony-language-server.log';
				let logStream = fs.createWriteStream(logFile, { flags: 'w' });

				process.stdout.pipe(logStream);
				process.stderr.pipe(logStream);

				console.log(`Storing log in '${logFile}'`);
			});
		});
	};
	*/

	// Options to control the language client
	let clientOptions: LanguageClientOptions = {
		// Register the server for plain text documents
		documentSelector: [{ scheme: 'file', language: 'plaintext'}, { scheme: 'file', language: 'javascript'}],
		synchronize: {
			// Synchronize the setting section 'glottonyLanguageServer' to the server
			configurationSection: 'glottonyLanguageServer',
			// Notify the server about file changes to '.glot files contain in the workspace
			fileEvents: workspace.createFileSystemWatcher('**/*')
		}
	}

	let client = new LanguageClient('glottonyLanguageServer', 'Glottony Language Server', serverOptions, clientOptions);
	client.registerProposedFeatures()
	console.log(client)

	// Create the language client and start the client.
	let disposable = client.start();	

	// Push the disposable to the context's subscriptions so that the 
	// client can be deactivated on extension deactivation
	context.subscriptions.push(disposable);

	// Use the console to output diagnostic information (console.log) and errors (console.error)
	// This line of code will only be executed once when your extension is activated
	console.log('Congratulations, your extension "glottony-language-server" is now active!');
}



// this method is called when your extension is deactivated
export function deactivate() {}
