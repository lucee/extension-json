component extends="org.lucee.cfml.test.LuceeTestCase" labels="jsonExt" {

	function beforeAll() {
		
	}

	function run( testResults, testBox )  {
		local.testsPath = getDirectoryFromPath(getCurrentTemplatePath()) & "JSON-Schema-Test-Suite-main\tests\draft2020-12";
		local.jsonTests = directoryList( local.testsPath );
		// systemOutput(local.jsonTests, true);

		// local.jsonTests =  [local.jsonTests[1]];
		//local.jsonTests =  ArrayMid(local.jsonTests, 1 , 3 );

		loop array=#local.jsonTests# item="local.jsonTestSuite"  {
			if ( fileExists( local.jsonTestSuite ) ) { // skip the optional folder
				local.name = listLast( local.jsonTestSuite , '/\' );
				describe( "Testcase for JSONValidate [#name#]", function() {
					local.json = deserializeJson( FileRead( jsonTestSuite ) );
					loop array=#local.json# item="local.jsonTest"  {
						//systemOutput( "-------#local.jsonTest.description#-------------------" , true );
						it(	title="Test [#listLast( jsonTestSuite , '/\' )#] / [#local.jsonTest.description#]", 
							data={
								jsonTest: jsonTest,
								jsonTestSuite: jsonTestSuite
							},
							body=function( data ) localmode=true {
								//systemOutput( data.jsonTestSuite , true );
								//systemOutput( data.jsonTest.description , true );
								schema = data.jsonTest.schema.toJson();
								tests = data.jsonTest.tests;
								for ( test in tests ){
									if ( !isNull( test.data ) ) 
										testData = test.data.toJson();
									else
										testData = "null"; // nulls in cfml are a bit different eh?
									var result = validateJson( json=testData, schema=schema );
									// systemOutput( result, true );
									expect( result.isValid  ).toBe( test.valid, test.description );
									// systemOutput( "" , true );
								}
							}
						);
						//systemOutput( "" , true );
					}
				});
				//systemOutput( "--------------------------" , true );
			}
		}
		/*
		function run( testResults, testBox ){

			
			// loop over test metadata
			for ( var thing in dynamicSuiteConfig ) {
				describe("Dynamic Suite #thing#", function(){
					// notice how data is passed into the it() closure:
					//  * data={ keyA=valueA, keyB=ValueB }
					//  * function( data )
					it( title=thing & "test", 
						data={ thing=thing }, 
						body=function( data ) {
						  var thing = data.thing;
						  expect( thing ).toBe( thing );
					});
				});
			}
	
		}
		*/
	}

	function afterAll() {

	}
}
